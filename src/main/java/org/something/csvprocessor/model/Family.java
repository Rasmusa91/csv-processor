package org.something.csvprocessor.model;

import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.csv.CsvLineReader;
import org.something.csvprocessor.xml.XmlWriter;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Model that represent a family with a CSV line reader and an XML writer
 */
public class Family {
  public final static CsvLineReader<Family> CSV_LINE_READER = csv ->
    Try.success(new Family(csv.getPart(1).toOptional(), csv.getPart(2).toOptional()));

  public final static XmlWriter<Family> xmlWriter = (document, family) -> {
    Element familyElement = document.createElement("family");

    family.name.ifPresent(name -> {
      Element element = document.createElement("name");
      element.setTextContent(name);
      familyElement.appendChild(element);
    });

    family.born.ifPresent(born -> {
      Element element = document.createElement("born");
      element.setTextContent(born);
      familyElement.appendChild(element);
    });

    family.phones.forEach(p -> familyElement.appendChild(Phone.xmlWriter.write(document, p)));
    family.addresses.forEach(a -> familyElement.appendChild(Address.xmlWriter.write(document, a)));

    return familyElement;
  };

  private final Optional<String> name;
  private final Optional<String> born;

  private final ArrayList<Phone> phones;
  private final ArrayList<Address> addresses;

  public Family(Optional<String> name, Optional<String> born) {
    this.name = name;
    this.born = born;

    phones = new ArrayList<>();
    addresses = new ArrayList<>();
  }

  public Family withPhone(Phone phone) {
    phones.add(phone);
    return this;
  }

  public Family withAddress(Address address) {
    addresses.add(address);
    return this;
  }

  public Optional<String> getName() {
    return name;
  }

  public Optional<String> getBorn() {
    return born;
  }

  public ArrayList<Address> getAddresses() {
    return addresses;
  }

  public ArrayList<Phone> getPhones() {
    return phones;
  }
}
