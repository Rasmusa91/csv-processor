package org.something.csvprocessor.model;

import org.something.csvprocessor.csv.CsvBatchReader;
import org.something.csvprocessor.csv.CsvLine;
import org.something.csvprocessor.csv.CsvLineReader;
import org.something.csvprocessor.csv.PrefixedCsvBatcher;
import org.something.csvprocessor.exception.CsvProcessorException;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.xml.XmlWriter;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Model that represent a person with a CSV line reader, a CSV batch reader, an XML writer
 */
public class Person {
  public final static CsvLineReader<Person> CSV_LINE_READER = csv ->
    Try.success(new Person(csv.getPart(1).toOptional(), csv.getPart(2).toOptional()));

  public static final class PersonCsvBatcher extends PrefixedCsvBatcher {
    public PersonCsvBatcher() {
      super("P");
    }
  }

  public static class PersonCsvBatchReader implements CsvBatchReader<Person> {
    /**
     * Recursively read a CSV line buffer by identifying the person at the first line,
     *   then reading line by line to append the person with information depending on the lines content.
     *   If a family is encountered, a similar recursive behavior is applied.
     */
    @Override
    public Try<Person> read(ArrayList<Try<CsvLine>> csvLines) {
      if (csvLines.size() > 0) {
        return csvLines.get(0)
          .flatMap(csv -> csv.getPart(0).flatMap(prefix -> switch (prefix.toLowerCase()) {
            case "p" -> csv.as(Person.CSV_LINE_READER)
              .flatMap(person -> read(person, csvLines, 1));
            default -> Try.failure(new CsvProcessorException("Group must start with a line that has prefix [P], found [" + prefix + "]"));
          }));
      } else {
        return Try.failure(new CsvProcessorException("Found unexpected empty group of CSV"));
      }
    }

    private Try<Person> read(Person person, ArrayList<Try<CsvLine>> csvLines, int index) {
      if (csvLines.size() > index) {
        return csvLines.get(index)
          .flatMap(csv -> csv.getPart(0).flatMap(prefix -> switch (prefix.toLowerCase()) {
            case "a" -> csv.as(Address.CSV_LINE_READER)
              .map(person::withAddress)
              .flatMap(p -> read(p, csvLines, index + 1));
            case "f" -> csv.as(Family.CSV_LINE_READER)
              .flatMap(f -> read(person, f, csvLines, index + 1));
            case "t" -> csv.as(Phone.CSV_LINE_READER)
              .map(person::withPhone)
              .flatMap(p -> read(p, csvLines, index + 1));
            default -> Try.failure(new CsvProcessorException("Unsupported prefix [" + prefix+ "]"));
          }));
      } else {
        return Try.success(person);
      }
    }

    private Try<Person> read(Person person, Family family, ArrayList<Try<CsvLine>> csvLines, int index) {
      if (csvLines.size() > index) {
        return csvLines.get(index)
          .flatMap(csv -> csv.getPart(0).flatMap(prefix -> switch (prefix.toLowerCase()) {
            case "a" -> csv.as(Address.CSV_LINE_READER)
              .map(family::withAddress)
              .flatMap(f -> read(person, f, csvLines, index + 1));
            case "f" -> read(person.withFamily(family), csvLines, index);
            case "t" -> csv.as(Phone.CSV_LINE_READER)
              .map(family::withPhone)
              .flatMap(f -> read(person, family, csvLines, index + 1));
            default -> Try.failure(new CsvProcessorException("Unsupported prefix [" + csv.getPart(0) + "]"));
          }));
      } else {
        return Try.success(person.withFamily(family));
      }
    }
  }

  public static XmlWriter<Person> xmlWriter = (document, person) -> {
    Element personElement = document.createElement("person");

    person.firstName.ifPresent(firstName -> {
      Element element = document.createElement("firstname");
      element.setTextContent(firstName);
      personElement.appendChild(element);
    });

    person.lastName.ifPresent(lastName -> {
      Element element = document.createElement("lastname");
      element.setTextContent(lastName);
      personElement.appendChild(element);
    });

    person.phones.forEach(p -> personElement.appendChild(Phone.xmlWriter.write(document, p)));
    person.addresses.forEach(a -> personElement.appendChild(Address.xmlWriter.write(document, a)));
    person.families.forEach(f -> personElement.appendChild(Family.xmlWriter.write(document, f)));

    return personElement;
  };

  private final Optional<String> firstName;
  private final Optional<String> lastName;

  private final ArrayList<Phone> phones;
  private final ArrayList<Address> addresses;
  private final ArrayList<Family> families;

  public Person(Optional<String> firstName, Optional<String> lastName) {
    this.firstName = firstName;
    this.lastName = lastName;

    this.phones = new ArrayList<>();
    this.addresses = new ArrayList<>();
    this.families = new ArrayList<>();
  }

  public Person withPhone(Phone phone) {
    phones.add(phone);
    return this;
  }

  public Person withAddress(Address address) {
    addresses.add(address);
    return this;
  }

  public Person withFamily(Family family) {
    families.add(family);
    return this;
  }

  public Optional<String> getFirstName() {
    return firstName;
  }

  public Optional<String> getLastName() {
    return lastName;
  }

  public ArrayList<Address> getAddresses() {
    return addresses;
  }

  public ArrayList<Phone> getPhones() {
    return phones;
  }
}

