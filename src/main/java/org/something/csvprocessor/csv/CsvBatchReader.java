package org.something.csvprocessor.csv;

import org.something.csvprocessor.monad.Try;

import java.util.ArrayList;

/**
 * Interface describing how to read a batch of CsvLine to T
 */
public interface CsvBatchReader<T> {
  Try<T> read(ArrayList<Try<CsvLine>> csvLines);
}
