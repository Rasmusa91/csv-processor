package org.something.csvprocessor.test.processing;

import org.something.csvprocessor.exception.CsvProcessorException;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.processing.XmlWriterMaterializer;
import org.something.csvprocessor.test.FileUtils;
import org.something.csvprocessor.xml.XmlWriter;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class XmlWriterMaterializerTest {
  @Test
  public void successfullyMaterializeXmlToFile() throws Exception {
    File file = Files.createTempFile("tmp", "tmp").toFile();
    XmlWriter<String> writer = Document::createElement;
    XmlWriterMaterializer<String> materializer = new XmlWriterMaterializer<>(file, writer, "strings");
    materializer.materialize(Try.success("a")).get();
    materializer.materialize(Try.success("b")).get();
    materializer.materialize(Try.success("c")).get();
    materializer.result().get();

    ArrayList<String> expected = new ArrayList<>();
    expected.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    expected.add("<strings>");
    expected.add("<a/>");
    expected.add("<b/>");
    expected.add("<c/>");
    expected.add("</strings>");

    FileUtils.verifyFile(expected, file);
  }

  @Test
  public void shouldPropagateAnyFailures() throws Exception {
    File file = Files.createTempFile("tmp", "tmp").toFile();
    XmlWriter<String> writer = Document::createElement;

    XmlWriterMaterializer<String> materializer = new XmlWriterMaterializer<>(file, writer, "strings");

    Exception thrown = assertThrows(Exception.class, () -> materializer.materialize(Try.failure(new CsvProcessorException("some-exception"))).get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: some-exception");
  }
}
