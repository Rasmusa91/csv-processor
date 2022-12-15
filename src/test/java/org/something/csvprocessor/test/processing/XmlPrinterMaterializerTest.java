package org.something.csvprocessor.test.processing;

import org.something.csvprocessor.exception.CsvProcessorException;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.processing.XmlPrinterMaterializer;
import org.something.csvprocessor.xml.XmlWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class XmlPrinterMaterializerTest {
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream tmpOutputStream = System.out;

  @Before
  public void before() {
    System.setOut(new PrintStream(outputStream));
  }

  @After
  public void after() {
    System.setOut(tmpOutputStream);
  }

  @Test
  public void successfullyMaterializeXmlToConsole() throws ParserConfigurationException, ExecutionException, InterruptedException {
    XmlWriter<String> writer = Document::createElement;

    XmlPrinterMaterializer<String> materializer = new XmlPrinterMaterializer<>(writer, "strings");
    materializer.materialize(Try.success("a")).get();
    materializer.materialize(Try.success("b")).get();
    materializer.materialize(Try.success("c")).get();
    materializer.result().get();

    String[] actual = outputStream.toString().split(System.lineSeparator());

    assertEquals(actual[0].trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    assertEquals(actual[1].trim(), "<strings>");
    assertEquals(actual[2].trim(), "<a/>");
    assertEquals(actual[3].trim(), "<b/>");
    assertEquals(actual[4].trim(), "<c/>");
    assertEquals(actual[5].trim(), "</strings>");
  }

  @Test
  public void shouldPropagateAnyFailures() throws ParserConfigurationException {
    XmlWriter<String> writer = Document::createElement;

    XmlPrinterMaterializer<String> materializer = new XmlPrinterMaterializer<>(writer, "strings");

    Exception thrown = assertThrows(Exception.class, () -> materializer.materialize(Try.failure(new CsvProcessorException("some-exception"))).get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: some-exception");
  }
}
