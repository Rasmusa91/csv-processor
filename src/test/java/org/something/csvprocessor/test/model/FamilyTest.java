package org.something.csvprocessor.test.model;

import org.something.csvprocessor.csv.CsvLine;
import org.something.csvprocessor.model.Address;
import org.something.csvprocessor.model.Family;
import org.something.csvprocessor.model.Phone;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.test.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Element;

import java.util.Optional;

import static org.junit.Assert.*;

public class FamilyTest {
  @Test
  public void fullFamilyFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "F", "Victoria", "1977" });
    Try<Family> maybeFamily = Family.CSV_LINE_READER.read(csvLine);

    assertFalse(maybeFamily.isFailure());
    assertTrue(maybeFamily.get().getName().isPresent());
    assertTrue(maybeFamily.get().getBorn().isPresent());

    assertEquals(maybeFamily.get().getName().get(), "Victoria");
    assertEquals(maybeFamily.get().getBorn().get(), "1977");
  }

  @Test
  public void emptyFamilyFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "F" });
    Try<Family> maybeFamily = Family.CSV_LINE_READER.read(csvLine);

    assertFalse(maybeFamily.isFailure());
    assertFalse(maybeFamily.get().getName().isPresent());
    assertFalse(maybeFamily.get().getBorn().isPresent());
  }

  @Test
  public void partialFamilyFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "F", "Victoria" });
    Try<Family> maybeFamily = Family.CSV_LINE_READER.read(csvLine);

    assertFalse(maybeFamily.isFailure());
    assertTrue(maybeFamily.get().getName().isPresent());
    assertFalse(maybeFamily.get().getBorn().isPresent());

    assertEquals(maybeFamily.get().getName().get(), "Victoria");
  }

  @Test
  public void fullFamilyToXml() throws Throwable {
    Family family = new Family(Optional.of("Victoria"), Optional.of("1977"))
      .withAddress(new Address(Optional.of("Haga Slott"), Optional.of("Stockholm"), Optional.of("10002")))
      .withPhone(new Phone(Optional.of("0768-101801"), Optional.of("08-101801")));

    Element element = Family.xmlWriter.write(XmlUtils.createDocument(), family);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<family><name>Victoria</name><born>1977</born><phone><mobile>0768-101801</mobile><landline>08-101801</landline></phone><address><street>Haga Slott</street><city>Stockholm</city><zipcode>10002</zipcode></address></family>";

    assertEquals(expected, actual);
  }

  @Test
  public void emptyFamilyToXml() throws Throwable {
    Family family = new Family(Optional.empty(), Optional.empty());
    Element element = Family.xmlWriter.write(XmlUtils.createDocument(), family);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<family/>";

    assertEquals(expected, actual);
  }

  @Test
  public void partialFamilyToXml() throws Throwable {
    Family family = new Family(Optional.of("Victoria"), Optional.empty());
    Element element = Family.xmlWriter.write(XmlUtils.createDocument(), family);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<family><name>Victoria</name></family>";

    assertEquals(expected, actual);
  }
}
