package org.something.csvprocessor.processing;

import org.something.csvprocessor.exception.CsvProcessorException;
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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.concurrent.CompletableFuture;

/**
 * Materializer that writes element objects as XML to sibling files element by element to remove the need
 * for keeping them all in memory before flushing them to file
 */
public class XmlWriterMaterializer <T> implements Materializer<Try<T>, Void> {

  private final XmlWriter<T> writer;
  private final String rootElement;
  private final BufferedWriter bufferedWriter;
  private final Document document;

  private final Transformer transformer;

  public XmlWriterMaterializer(File destinationFile, XmlWriter<T> writer, String rootElement) throws CsvProcessorException, IOException, ParserConfigurationException, TransformerConfigurationException {
    this.writer = writer;
    this.rootElement = rootElement;

    if (destinationFile.exists() && !destinationFile.delete()) {
      throw new CsvProcessorException("Could not delete destination file [" + destinationFile.getPath() + "]");
    }

    if (!destinationFile.createNewFile()) {
      throw new CsvProcessorException("Could not create destination file [" + destinationFile.getPath() + "]");
    }

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    this.document = docBuilder.newDocument();

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    this.transformer = transformerFactory.newTransformer();
    this.transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    this.transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

    // Write the XML header immediately to let the materializer append the file with elements when it receives one
    this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destinationFile)));
    this.bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    this.bufferedWriter.newLine();
    this.bufferedWriter.write("<" + this.rootElement + ">");
    this.bufferedWriter.newLine();
  }

  /**
   * Serialize the received element as XML and append the destination file
   * @param t The received element
   * @return Returns a completed completable future either empty or with any potential errors
   *  that will be propagated to stream drainage
   */
  @Override
  public CompletableFuture<Void> materialize(Try<T> t) {
    return Try.<Void>of(() -> {
      Element element = this.writer.write(document, t.get());
      transformer.transform(new DOMSource(element), new StreamResult(bufferedWriter));
      return null;
    }).toCompletableFuture();
  }

  /**
   * Finalize the file by appending the enclosing tag
   * @return Returns a completed completable future either empty or with any potential errors
   *  that will be propagated to stream drainage
   */
  @Override
  public CompletableFuture<Void> result() {
    return Try.<Void>of(() -> {
      bufferedWriter.write("</" + this.rootElement + ">");
      bufferedWriter.flush();
      bufferedWriter.close();
      return null;
    }).toCompletableFuture();
  }
}
