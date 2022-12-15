package org.something.csvprocessor.csv;

import org.something.csvprocessor.monad.Try;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Interface describing how to batch a CsvLine one by one
 */
public interface CsvBatcher {
  /**
   * Buffer the CsvLine until some predicate is met.
   * If the batching is in progress, return an empty optional
   * If the batch is completed, return a non-empty optional with the resulting batch
   */
  Optional<ArrayList<Try<CsvLine>>> batch(Try<CsvLine> csvLine);

  /**
   * Should return the current state of the batch
   */
  ArrayList<Try<CsvLine>> batch();
}
