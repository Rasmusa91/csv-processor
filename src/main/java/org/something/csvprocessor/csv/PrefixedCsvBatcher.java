package org.something.csvprocessor.csv;

import org.something.csvprocessor.csv.CsvBatcher;
import org.something.csvprocessor.csv.CsvLine;
import org.something.csvprocessor.monad.Try;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Batches CSV lines until a specified prefix (at index 0) is found.
 */
public class PrefixedCsvBatcher implements CsvBatcher {
  private final String prefix;

  public PrefixedCsvBatcher(String prefix) {
    this.prefix = prefix;
  }

  private ArrayList<Try<CsvLine>> buffer = new ArrayList<>();

  @Override
  public Optional<ArrayList<Try<CsvLine>>> batch(Try<CsvLine> csvLine) {
    if (buffer.isEmpty() || !csvLine.flatMap(_csvLine -> _csvLine.getPart(0)).exists(head -> head.equalsIgnoreCase(prefix))) {
      buffer.add(csvLine);
      return Optional.empty();
    } else {
      ArrayList<Try<CsvLine>> tmp = buffer;

      buffer = new ArrayList<>();
      buffer.add(csvLine);

      return Optional.of(tmp);
    }
  }

  @Override
  public ArrayList<Try<CsvLine>> batch() {
    return buffer;
  }
}
