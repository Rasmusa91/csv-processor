package org.something.csvprocessor.processing;

import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.processing.Materializer;
import org.something.csvprocessor.xml.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;

/**
 * Materializer that aggregates all elements as a single XML document and prints the result to the console
 */
public class XmlPrinterMaterializer<T> implements Materializer<Try<T>, Void> {
  private final XmlWriter<T> writer;

  private final Document document;
  private final Element rootElement;

  public XmlPrinterMaterializer(XmlWriter<T> writer, String rootElement) throws ParserConfigurationException {
    this.writer = writer;

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

    this.document = docBuilder.newDocument();
    this.rootElement = this.document.createElement(rootElement);
    this.document.appendChild(this.rootElement);
  }

  /**
   * Serialize the received element as XML and append it to the XML document
   * @param element The received element to be serialized
   * @return Returns a completed completable future either empty or with any potential errors
   *   that will be propagated to stream drainage
   */
  @Override
  public CompletableFuture<Void> materialize(Try<T> element) {
    return element.<Void>map(_element -> {
      this.rootElement.appendChild(writer.write(document, _element));
      return null;
    }).toCompletableFuture();
  }

  /**
   * Finalize by writing the aggregated XML document to the console
   * @return Returns a completed completable future either empty or with any potential errors
   *   that will be propagated to stream drainage
   */
  @Override
  public CompletableFuture<Void> result() {
    return Try.<Void>of(() -> {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(document), new StreamResult(writer));

      System.out.println(writer);

      return null;
    }).toCompletableFuture();
  }
}