package org.something.csvprocessor.test.csv;

import org.something.csvprocessor.csv.CsvLine;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class CsvLineTest {
  @Test
  public void successfullyQueryCsvLine() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[]{ "a", "b", "c" });
    Assert.assertEquals(csvLine.getPart(0).get(), "a");
    Assert.assertEquals(csvLine.getPart(1).get(), "b");
    Assert.assertEquals(csvLine.getPart(2).get(), "c");
    assertTrue(csvLine.getPart(3).isFailure());
  }

  @Test
  public void successfullyParseCsvLine() throws Throwable {
    CsvLine csvLine = CsvLine.parseUnsafe("a|b|c", "|");
    Assert.assertEquals(csvLine.getPart(0).get(), "a");
    Assert.assertEquals(csvLine.getPart(1).get(), "b");
    Assert.assertEquals(csvLine.getPart(2).get(), "c");
    assertTrue(csvLine.getPart(3).isFailure());
  }
}
