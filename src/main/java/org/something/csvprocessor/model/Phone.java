package org.something.csvprocessor.model;

import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.csv.CsvLineReader;
import org.something.csvprocessor.xml.XmlWriter;
import org.w3c.dom.Element;

import java.util.Optional;

/**
 * Model that represent a phone with a CSV line reader and an XML writer
 */
public final class Phone {
  public final static CsvLineReader<Phone> CSV_LINE_READER = csv ->
    Try.success(new Phone(csv.getPart(1).toOptional(), csv.getPart(2).toOptional()));

  public final static XmlWriter<Phone> xmlWriter = (document, phone) -> {
    Element phoneElement = document.createElement("phone");

    phone.mobile.ifPresent(mobileNumber -> {
      Element element = document.createElement("mobile");
      element.setTextContent(mobileNumber);
      phoneElement.appendChild(element);
    });

    phone.landline.ifPresent(landlineNumber -> {
      Element element = document.createElement("landline");
      element.setTextContent(landlineNumber);
      phoneElement.appendChild(element);
    });

    return phoneElement;
  };
  private final Optional<String> mobile;
  private final Optional<String> landline;

  public Phone(Optional<String> mobile, Optional<String> landline) {
    this.mobile = mobile;
    this.landline = landline;
  }

  public Optional<String> getMobile() {
    return mobile;
  }

  public Optional<String> getLandline() {
    return landline;
  }
}
