package org.something.csvprocessor.model;

import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.csv.CsvLineReader;
import org.something.csvprocessor.xml.XmlWriter;
import org.w3c.dom.Element;

import java.util.Optional;

/**
 * Model that represent an address with a CSV line reader and an XML writer
 */
public final class Address {
  public final static CsvLineReader<Address> CSV_LINE_READER = csv ->
    Try.success(new Address(csv.getPart(1).toOptional(), csv.getPart(2).toOptional(), csv.getPart(3).toOptional()));

  public final static XmlWriter<Address> xmlWriter = (document, address) -> {
    Element addressElement = document.createElement("address");

    address.street.ifPresent(street -> {
      Element element = document.createElement("street");
      element.setTextContent(street);
      addressElement.appendChild(element);
    });

    address.city.ifPresent(street -> {
      Element element = document.createElement("city");
      element.setTextContent(street);
      addressElement.appendChild(element);
    });

    address.zipCode.ifPresent(zipCode -> {
      Element element = document.createElement("zipcode");
      element.setTextContent(zipCode);
      addressElement.appendChild(element);
    });

    return addressElement;
  };



  private final Optional<String> street;
  private final Optional<String> city;
  private final Optional<String> zipCode;

  public Address(Optional<String> street, Optional<String> city, Optional<String> zipCode) {
    this.street = street;
    this.city = city;
    this.zipCode = zipCode;
  }

  public Optional<String> getStreet() {
    return street;
  }

  public Optional<String> getCity() {
    return city;
  }

  public Optional<String> getZipCode() {
    return zipCode;
  }
}
