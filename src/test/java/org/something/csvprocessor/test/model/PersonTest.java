package org.something.csvprocessor.test.model;

import org.something.csvprocessor.csv.CsvLine;
import org.something.csvprocessor.model.Address;
import org.something.csvprocessor.model.Family;
import org.something.csvprocessor.model.Person;
import org.something.csvprocessor.model.Phone;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.test.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Element;

import java.util.Optional;

import static org.junit.Assert.*;

public class PersonTest {
  @Test
  public void fullPersonFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "P", "Carl Gustaf", "Bernadotte" });
    Try<Person> maybePerson = Person.CSV_LINE_READER.read(csvLine);

    assertFalse(maybePerson.isFailure());
    assertTrue(maybePerson.get().getFirstName().isPresent());
    assertTrue(maybePerson.get().getLastName().isPresent());

    assertEquals(maybePerson.get().getFirstName().get(), "Carl Gustaf");
    assertEquals(maybePerson.get().getLastName().get(), "Bernadotte");
  }

  @Test
  public void emptyPersonFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "P" });
    Try<Person> maybePerson = Person.CSV_LINE_READER.read(csvLine);

    assertFalse(maybePerson.isFailure());
    assertFalse(maybePerson.get().getFirstName().isPresent());
    assertFalse(maybePerson.get().getLastName().isPresent());
  }

  @Test
  public void partialPersonFromCsv() throws Throwable {
    CsvLine csvLine = new CsvLine(new String[] { "P", "Carl Gustaf" });
    Try<Person> maybePerson = Person.CSV_LINE_READER.read(csvLine);

    assertFalse(maybePerson.isFailure());
    assertTrue(maybePerson.get().getFirstName().isPresent());
    assertFalse(maybePerson.get().getLastName().isPresent());

    assertEquals(maybePerson.get().getFirstName().get(), "Carl Gustaf");
  }

  @Test
  public void fullPersonToXml() throws Throwable {
    Person person = new Person(Optional.of("Carl Gustaf"), Optional.of("Bernadotte"))
      .withAddress(new Address(Optional.of("Drottningholms slott"), Optional.of("Stockholm"), Optional.of("10001")))
      .withPhone(new Phone(Optional.of("0768-101801"), Optional.of("08-101801")))
      .withFamily(new Family(Optional.of("Victoria"), Optional.of("1977"))
        .withAddress(new Address(Optional.of("Haga Slott"), Optional.of("Stockholm"), Optional.of("10002")))
        .withPhone(new Phone(Optional.of("0768-101801"), Optional.of("08-101801"))));

    Element element = Person.xmlWriter.write(XmlUtils.createDocument(), person);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<person><firstname>Carl Gustaf</firstname><lastname>Bernadotte</lastname><phone><mobile>0768-101801</mobile><landline>08-101801</landline></phone><address><street>Drottningholms slott</street><city>Stockholm</city><zipcode>10001</zipcode></address><family><name>Victoria</name><born>1977</born><phone><mobile>0768-101801</mobile><landline>08-101801</landline></phone><address><street>Haga Slott</street><city>Stockholm</city><zipcode>10002</zipcode></address></family></person>";

    assertEquals(expected, actual);
  }

  @Test
  public void emptyPersonToXml() throws Throwable {
    Person person = new Person(Optional.empty(), Optional.empty());
    Element element = Person.xmlWriter.write(XmlUtils.createDocument(), person);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<person/>";

    assertEquals(expected, actual);
  }

  @Test
  public void partialPersonToXml() throws Throwable {
    Person person = new Person(Optional.of("Carl Gustaf"), Optional.empty());
    Element element = Person.xmlWriter.write(XmlUtils.createDocument(), person);

    String actual = XmlUtils.nodeToString(element);
    String expected = "<person><firstname>Carl Gustaf</firstname></person>";

    assertEquals(expected, actual);
  }
}
