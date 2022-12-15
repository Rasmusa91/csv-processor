package org.something.csvprocessor.csv;

import org.something.csvprocessor.exception.CsvProcessorException;
import org.something.csvprocessor.monad.Try;

import java.util.regex.Pattern;

/**
 * Convenience class for parsing and getting parts from CSV line
 */
public class CsvLine {
  public static Try<CsvLine> parse(String raw, String separator) {
    return Try.of(() -> parseUnsafe(raw, separator));
  }

  public static CsvLine parseUnsafe(String raw, String separator) {
    String[] parts = Pattern.compile(separator, Pattern.LITERAL).split(raw);
    return new CsvLine(parts);
  }

  private final String[] parts;

  public CsvLine(String[] parts) {
    this.parts = parts;
  }

  public Try<String> getPart(int index) {
    if (parts.length > index) {
      return Try.success(parts[index]);
    } else {
      return Try.failure(new CsvProcessorException("Could not find part at index [" + index + "]"));
    }
  }

  public <T> Try<T> as(CsvLineReader<T> reader) {
    return reader.read(this);
  }
}
