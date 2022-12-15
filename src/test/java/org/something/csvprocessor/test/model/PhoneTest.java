package org.something.csvprocessor.test.model;

import org.something.csvprocessor.csv.CsvLine;
import org.something.csvprocessor.model.Phone;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.test.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Element;

import java.util.Optional;

import static org.junit.Assert.*;

public class PhoneTest {
  @Test
  public void fullPhoneFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "T", "0768-101801", "08-101801" });
    Try<Phone> maybePhone = Phone.CSV_LINE_READER.read(csvLine);

    assertFalse(maybePhone.isFailure());
    assertTrue(maybePhone.get().getMobile().isPresent());
    assertTrue(maybePhone.get().getLandline().isPresent());

    assertEquals(maybePhone.get().getMobile().get(), "0768-101801");
    assertEquals(maybePhone.get().getLandline().get(), "08-101801");
  }

  @Test
  public void emptyPhoneFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "T" });
    Try<Phone> maybePhone = Phone.CSV_LINE_READER.read(csvLine);

    assertFalse(maybePhone.isFailure());
    assertFalse(maybePhone.get().getMobile().isPresent());
    assertFalse(maybePhone.get().getLandline().isPresent());
  }

  @Test
  public void partialPhoneFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "T", "0768-101801" });
    Try<Phone> maybePhone = Phone.CSV_LINE_READER.read(csvLine);

    assertFalse(maybePhone.isFailure());
    assertTrue(maybePhone.get().getMobile().isPresent());
    assertFalse(maybePhone.get().getLandline().isPresent());

    assertEquals(maybePhone.get().getMobile().get(), "0768-101801");
  }

  @Test
  public void fullPhoneToXml() throws Throwable {
    Phone phone = new Phone(Optional.of("0768-101801"), Optional.of("08-101801"));
    Element element = Phone.xmlWriter.write(XmlUtils.createDocument(), phone);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<phone><mobile>0768-101801</mobile><landline>08-101801</landline></phone>";

    assertEquals(expected, actual);
  }

  @Test
  public void emptyPhoneToXml() throws Throwable {
    Phone phone = new Phone(Optional.empty(), Optional.empty());
    Element element = Phone.xmlWriter.write(XmlUtils.createDocument(), phone);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<phone/>";

    assertEquals(expected, actual);
  }

  @Test
  public void partialPhoneToXml() throws Throwable {
    Phone phone = new Phone(Optional.of("0768-101801"), Optional.empty());
    Element element = Phone.xmlWriter.write(XmlUtils.createDocument(), phone);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<phone><mobile>0768-101801</mobile></phone>";

    assertEquals(expected, actual);
  }
}
