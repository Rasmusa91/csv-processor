package org.something.csvprocessor.csv;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.processing.FileLineIterator;
import org.something.csvprocessor.processing.Materializer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Streams various sources via a CSV processing stream with a given CSV batch reader
 *   that should decide how the streamed lines are parsed and batched.
 *   The batched result is then materialized decided by the given materializer.
 */
public class CsvBatchProcessor {
  private final String separator;
  private final ActorSystem system;
  private final int parallelism;

  public CsvBatchProcessor(String separator, ActorSystem system, int parallelism) {
    this.separator = separator;
    this.system = system;
    this.parallelism = parallelism;
  }

  /**
   * Processes a given CSV file with the given batcher, batchReader and materializer
   */
  public <T, U> CompletableFuture<U> process(File file, CsvBatcher batcher, CsvBatchReader<T> batchReader, Materializer<Try<T>, U> materializer) {
    return Try.of(() -> new FileLineIterator(file))
      .toCompletableFuture()
      .thenCompose(iterator -> process(iterator, batcher, batchReader, materializer));
  }

  /**
   * Processes a given FileLineIterator with the given batcher, batchReader and materializer
   */
  public <T, U> CompletableFuture<U> process(FileLineIterator iterator, CsvBatcher batcher, CsvBatchReader<T> batchReader, Materializer<Try<T>, U> materializer) {
    return process(Source.fromIterator(() -> iterator), batcher, batchReader, materializer)
      .thenApply(result -> {
        iterator.close().logError();
        return result;
      });
  }

  /**
   * Processes a given array with the given batcher, batchReader and materializer
   */
  public <T, U> CompletableFuture<U> process(ArrayList<String> lines, CsvBatcher batcher, CsvBatchReader<T> batchReader, Materializer<Try<T>, U> materializer) {
    return process(Source.fromIterator(lines::iterator), batcher, batchReader, materializer);
  }

  /**
   * Processes a given Source with the given batcher, batchReader and materializer
   */
  public <T, U> CompletableFuture<U> process(Source<String, NotUsed> source, CsvBatcher batcher, CsvBatchReader<T> batchReader, Materializer<Try<T>, U> materializer) {
    return source
      .filterNot(String::isEmpty)
      .map(line -> CsvLine.parse(line, separator))
      .via(batchFlow(batcher))
      .mapAsync(parallelism, batch -> CompletableFuture.supplyAsync(() -> batchReader.read(batch)))
      .mapAsync(1, materializer::materialize) // Parallelism 1 to preserve the order
      .run(system)
      .toCompletableFuture()
      .thenCompose(Void -> materializer.materialize(batchReader.read(batcher.batch())))
      .thenCompose(Void -> materializer.result());
  }

  private <T> Flow<Try<CsvLine>, ArrayList<Try<CsvLine>>, NotUsed> batchFlow(CsvBatcher batcher) {
    return Flow.<Try<CsvLine>>create().mapConcat(csvLine -> {
      Optional<ArrayList<Try<CsvLine>>> result = batcher.batch(csvLine);
      if (result.isPresent()) {
        return Collections.singleton(result.get());
      } else {
        return Collections.emptyList();
      }
    });
  }
}
