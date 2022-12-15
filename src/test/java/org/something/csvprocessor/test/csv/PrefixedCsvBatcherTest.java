package org.something.csvprocessor.test.csv;

import org.junit.Test;
import org.something.csvprocessor.csv.CsvLine;
import org.something.csvprocessor.csv.PrefixedCsvBatcher;
import org.something.csvprocessor.monad.Try;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.*;

public class PrefixedCsvBatcherTest {
  @Test
  public void test() {
    PrefixedCsvBatcher batcher = new PrefixedCsvBatcher("A");

    verifyNotDone(batcher, new CsvLine(new String[] { "A" }));
    verifyNotDone(batcher, new CsvLine(new String[] { "B" }));
    verifyNotDone(batcher, new CsvLine(new String[] { "B" }));
    verifyNotDone(batcher, new CsvLine(new String[] { "B" }));
    verifyDone(batcher, new CsvLine(new String[] { "A" }), 4);
    verifyDone(batcher, new CsvLine(new String[] { "A" }), 1);
    verifyNotDone(batcher, new CsvLine(new String[] { "B" }));
    assertEquals(batcher.batch().size(), 2);
  }

  private void verifyNotDone(PrefixedCsvBatcher batcher, CsvLine csvLine) {
    assertFalse(batcher.batch(Try.success(csvLine)).isPresent());
  }

  private void verifyDone(PrefixedCsvBatcher batcher, CsvLine csvLine, int expectedSize) {
    Optional<ArrayList<Try<CsvLine>>> result = batcher.batch(Try.success(csvLine));

    assertTrue(result.isPresent());
    assertEquals(result.get().size(), expectedSize);
  }
}
