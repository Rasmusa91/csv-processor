package org.something.csvprocessor.component;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.something.csvprocessor.csv.CsvBatchProcessor;
import org.something.csvprocessor.csv.CsvBatchReader;
import org.something.csvprocessor.csv.CsvBatcher;
import org.something.csvprocessor.exception.CsvProcessorException;
import org.something.csvprocessor.model.Person;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.processing.Materializer;
import org.something.csvprocessor.processing.XmlWriterMaterializer;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Component that reads one or more CSV files, converts it to XML and writes the result to a new sibling file
 */
public class FileConverterComponent implements Component {
  private final String sourcePath;
  private final String separator;

  private final ActorSystem system;
  private final int parallelism;

  public FileConverterComponent(String sourcePath, String separator, ActorSystem system, int parallelism) {
    this.sourcePath = sourcePath;
    this.separator = separator;

    this.system = system;
    this.parallelism = parallelism;
  }

  /**
   * Create a source with all found files, stream the via the CSV batch processor, materialize the result
   *   by parsing the CSV to XML and write the result to a sibling file
   * @return The stream process as a completable future
   */
  @Override
  public CompletableFuture<Void> run() {
    File sourceFile = new File(sourcePath);

    if (sourceFile.exists()) {
      return fileSystemSource(sourceFile)
        .filter(file -> file.getPath().endsWith(".csv"))
        .mapAsync(parallelism, this::run)
        .run(system)
        .toCompletableFuture()
        .thenApply(Void -> null);
    } else {
      return CompletableFuture.failedFuture(new CsvProcessorException("Could not find file or directory at source path [" + sourcePath + "]"));
    }
  }

  private CompletableFuture<Void> run(File sourceFile) {
    File destinationFile = new File(sourceFile.getPath().replace(".csv", ".xml"));
    return Try.of(() ->  new XmlWriterMaterializer<>(destinationFile, Person.xmlWriter, "people"))
      .toCompletableFuture()
      .thenCompose(materialize -> run(sourceFile, materialize));
  }

  private CompletableFuture<Void> run(File sourceFile, Materializer<Try<Person>, Void> materializer) {
    CsvBatchProcessor processor = new CsvBatchProcessor(separator, system, parallelism);
    CsvBatchReader<Person> batchReader = new Person.PersonCsvBatchReader();
    CsvBatcher batcher = new Person.PersonCsvBatcher();
    return processor.process(sourceFile, batcher, batchReader, materializer);
  }

  /**
   * Recursively find all files as a stream source
   * @param file The root file or directory
   * @return The stream source with all files
   */
  private Source<File, NotUsed> fileSystemSource(File file) {
    if (file.isDirectory()) {
      return Source
        .fromIterator(() -> Arrays.stream(Objects.requireNonNull(file.listFiles())).iterator())
        .flatMapConcat(this::fileSystemSource);
    } else {
      return Source.single(file);
    }
  }

  @Override
  public CompletableFuture<Void> stop() {
    return CompletableFuture.completedFuture(null);
  }
}
