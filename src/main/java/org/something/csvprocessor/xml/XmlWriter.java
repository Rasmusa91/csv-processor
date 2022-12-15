package org.something.csvprocessor.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface describing how to read T to an XML element
 */
public interface XmlWriter<T> {
  Element write(Document document, T t);
}
