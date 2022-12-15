package org.something.csvprocessor.test.components;

import akka.actor.ActorSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.something.csvprocessor.component.FileConverterComponent;
import org.something.csvprocessor.exception.CsvProcessorException;
import org.something.csvprocessor.test.FileUtils;
import org.something.csvprocessor.test.SampleData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FileConverterComponentTest {
  private final ActorSystem system = ActorSystem.create();
  private File tmpDirectory;

  @Before
  public void before() throws IOException {
    tmpDirectory = Files.createTempDirectory("tmp").toFile();
  }

  @After
  public void after() {
    system.terminate();
  }

  @Test
  public void successfullyParseSingleFile() throws IOException, ExecutionException, InterruptedException {
    File sourceFile = new File(tmpDirectory.getPath() + "/" + UUID.randomUUID() + ".csv");
    FileUtils.writeFile(sourceFile, SampleData.sampleInputData1());

    new FileConverterComponent(sourceFile.getPath(), "|", system, 1).run().get();

    FileUtils.verifyFile(SampleData.sampleOutputData1(), new File(sourceFile.getPath().replace(".csv", ".xml")));
  }

  @Test
  public void successfullyParseNestedFiles() throws IOException, ExecutionException, InterruptedException {
    File sourceFile1 = new File(tmpDirectory.getPath() + "/" + UUID.randomUUID() + ".csv");
    FileUtils.writeFile(sourceFile1, SampleData.sampleInputData1());

    File sourceFile2 = new File(tmpDirectory.getPath() + "/" + UUID.randomUUID() + ".csv");
    FileUtils.writeFile(sourceFile2, SampleData.sampleInputData2());

    Files.createDirectory(Path.of(tmpDirectory.getPath() + "/nested"));
    File sourceFile3 = new File(tmpDirectory.getPath() + "/nested/" + UUID.randomUUID() + ".csv");
    FileUtils.writeFile(sourceFile3, SampleData.sampleInputData3());

    new FileConverterComponent(tmpDirectory.getPath(), "|", system, 1).run().get();

    FileUtils.verifyFile(SampleData.sampleOutputData1(), new File(sourceFile1.getPath().replace(".csv", ".xml")));
    FileUtils.verifyFile(SampleData.sampleOutputData2(), new File(sourceFile2.getPath().replace(".csv", ".xml")));
    FileUtils.verifyFile(SampleData.sampleOutputData3(), new File(sourceFile3.getPath().replace(".csv", ".xml")));
  }

  @Test
  public void successfullyParseSingleFileWithEmptyLines() throws IOException, ExecutionException, InterruptedException {
    File sourceFile = new File(tmpDirectory.getPath() + "/" + UUID.randomUUID() + ".csv");
    ArrayList<String> data = SampleData.sampleInputData1();
    data.add("");
    data.add("");
    data.add("");
    FileUtils.writeFile(sourceFile, data);

    new FileConverterComponent(sourceFile.getPath(), "|", system, 1).run().get();

    FileUtils.verifyFile(SampleData.sampleOutputData1(), new File(sourceFile.getPath().replace(".csv", ".xml")));
  }

  @Test
  public void failEmptyInput() throws IOException, CsvProcessorException {
    File sourceFile = new File(tmpDirectory.getPath() + "/" + UUID.randomUUID() + ".csv");

    if (!sourceFile.createNewFile()) {
      throw new CsvProcessorException("Could not create test file");
    }

    FileConverterComponent inputConverter = new FileConverterComponent(sourceFile.getPath(), "|", system, 1);

    ExecutionException thrown = assertThrows(ExecutionException.class, () -> inputConverter.run().get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: Found unexpected empty group of CSV");
  }

  @Test
  public void failInvalidRootLine() throws IOException {
    File sourceFile = new File(tmpDirectory.getPath() + "/" + UUID.randomUUID() + ".csv");

    ArrayList<String> input = new ArrayList<>();
    input.add("T|0768-101801|08-101801");

    FileUtils.writeFile(sourceFile, input);

    FileConverterComponent inputConverter = new FileConverterComponent(sourceFile.getPath(), "|", system, 1);

    Exception thrown = assertThrows(Exception.class, () -> inputConverter.run().get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: Group must start with a line that has prefix [P], found [T]");
  }

  @Test
  public void failUnsupportedPrefix() throws IOException {
    File sourceFile = new File(tmpDirectory.getPath() + "/" + UUID.randomUUID() + ".csv");

    ArrayList<String> input = new ArrayList<>();
    input.add("P|Carl Gustaf|Bernadotte");
    input.add("K|a|b|c");

    FileUtils.writeFile(sourceFile, input);

    FileConverterComponent inputConverter = new FileConverterComponent(sourceFile.getPath(), "|", system, 1);

    ExecutionException thrown = assertThrows(ExecutionException.class, () -> inputConverter.run().get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: Unsupported prefix [K]");
  }
}
