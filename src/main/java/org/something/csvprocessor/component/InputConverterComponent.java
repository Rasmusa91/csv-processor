package org.something.csvprocessor.component;

import akka.actor.ActorSystem;
import org.something.csvprocessor.csv.CsvBatchProcessor;
import org.something.csvprocessor.csv.CsvBatchReader;
import org.something.csvprocessor.csv.CsvBatcher;
import org.something.csvprocessor.model.Person;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.processing.Materializer;
import org.something.csvprocessor.processing.XmlPrinterMaterializer;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Component that receives an array of CSV lines to be parsed and outputs it to the console
 */
public class InputConverterComponent implements Component {
  private final ArrayList<String> lines;
  private final String separator;

  private final ActorSystem system;
  private final int parallelism;

  public InputConverterComponent(ArrayList<String> lines, String separator, ActorSystem system, int parallelism) {
    this.lines = lines;
    this.separator = separator;

    this.system = system;
    this.parallelism = parallelism;
  }

  /**
   * Streams the given lines with the CSV batch processor, materialize the result
   *   by parsing the CSV to XML and write the result to the console
   * @return The stream process as a completable future
   */
  @Override
  public CompletableFuture<Void> run() {
    return Try.of(() -> new XmlPrinterMaterializer<>(Person.xmlWriter, "people"))
      .toCompletableFuture()
      .thenCompose(this::run);
  }

  private CompletableFuture<Void> run(Materializer<Try<Person>, Void> materializer) {
    CsvBatchProcessor processor = new CsvBatchProcessor(separator, system, parallelism);
    CsvBatcher batcher = new Person.PersonCsvBatcher();
    CsvBatchReader<Person> batchReader = new Person.PersonCsvBatchReader();
    return processor.process(lines, batcher, batchReader, materializer);
  }

  @Override
  public CompletableFuture<Void> stop() {
    return CompletableFuture.completedFuture(null);
  }
}
