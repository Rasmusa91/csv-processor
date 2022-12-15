package org.something.csvprocessor.csv;

import org.something.csvprocessor.monad.Try;

/**
 * Interface describing how to read a CsvLine
 */
public interface CsvLineReader<T> {
  Try<T> read(CsvLine csvLine);
}
