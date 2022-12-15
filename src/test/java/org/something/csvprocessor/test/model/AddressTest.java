package org.something.csvprocessor.test.model;

import org.something.csvprocessor.csv.CsvLine;
import org.something.csvprocessor.model.Address;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.test.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Element;

import java.util.Optional;

import static org.junit.Assert.*;

public class AddressTest {
  @Test
  public void fullAddressFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "A", "Drottningholms slott", "Stockholm", "10001" });
    Try<Address> maybeAddress = Address.CSV_LINE_READER.read(csvLine);

    assertFalse(maybeAddress.isFailure());
    assertTrue(maybeAddress.get().getStreet().isPresent());
    assertTrue(maybeAddress.get().getCity().isPresent());
    assertTrue(maybeAddress.get().getZipCode().isPresent());

    assertEquals(maybeAddress.get().getStreet().get(), "Drottningholms slott");
    assertEquals(maybeAddress.get().getCity().get(), "Stockholm");
    assertEquals(maybeAddress.get().getZipCode().get(), "10001");
  }

  @Test
  public void emptyAddressFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "A" });
    Try<Address> maybeAddress = Address.CSV_LINE_READER.read(csvLine);

    assertFalse(maybeAddress.isFailure());
    assertFalse(maybeAddress.get().getStreet().isPresent());
    assertFalse(maybeAddress.get().getCity().isPresent());
    assertFalse(maybeAddress.get().getZipCode().isPresent());
  }

  @Test
  public void partialAddressFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "A", "Drottningholms slott" });
    Try<Address> maybeAddress = Address.CSV_LINE_READER.read(csvLine);

    assertFalse(maybeAddress.isFailure());
    assertTrue(maybeAddress.get().getStreet().isPresent());
    assertFalse(maybeAddress.get().getCity().isPresent());
    assertFalse(maybeAddress.get().getZipCode().isPresent());

    assertEquals(maybeAddress.get().getStreet().get(), "Drottningholms slott");
  }

  @Test
  public void fullAddressToXml() throws Throwable {
    Address address = new Address(Optional.of("Drottningholms slott"), Optional.of("Stockholm"), Optional.of("10001"));
    Element element = Address.xmlWriter.write(XmlUtils.createDocument(), address);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<address><street>Drottningholms slott</street><city>Stockholm</city><zipcode>10001</zipcode></address>";

    assertEquals(expected, actual);
  }

  @Test
  public void emptyAddressToXml() throws Throwable {
    Address address = new Address(Optional.empty(), Optional.empty(), Optional.empty());
    Element element = Address.xmlWriter.write(XmlUtils.createDocument(), address);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<address/>";

    assertEquals(expected, actual);
  }

  @Test
  public void partialAddressToXml() throws Throwable {
    Address address = new Address(Optional.of("Drottningholms slott"), Optional.empty(), Optional.empty());
    Element element = Address.xmlWriter.write(XmlUtils.createDocument(), address);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<address><street>Drottningholms slott</street></address>";

    assertEquals(expected, actual);
  }
}
