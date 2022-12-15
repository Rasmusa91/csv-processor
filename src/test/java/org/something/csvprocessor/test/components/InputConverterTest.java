package org.something.csvprocessor.test.components;

import akka.actor.ActorSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.something.csvprocessor.component.InputConverterComponent;
import org.something.csvprocessor.test.SampleData;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class InputConverterTest {
  private final ActorSystem system = ActorSystem.create();
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream tmpOutputStream = System.out;

  @Before
  public void before() {
    System.setOut(new PrintStream(outputStream));
  }

  @After
  public void after() {
    System.setOut(tmpOutputStream);
    system.terminate();
  }

  @Test
  public void runSuccessfully() throws ExecutionException, InterruptedException {
    InputConverterComponent inputConverterComponent = new InputConverterComponent(SampleData.sampleInputData1(), "|", system, 1);
    inputConverterComponent.run().get();

    ArrayList<String> expected = SampleData.sampleOutputData1();

    String[] actual = outputStream.toString().split(System.lineSeparator());

    assertEquals(actual.length, expected.size());

    for (int i = 0; i < actual.length; i++) {
      assertEquals(actual[i].trim(), expected.get(i));
    }
  }

  @Test
  public void runSuccessfullyIgnoreEmptyLines() throws ExecutionException, InterruptedException {
    ArrayList<String> data = SampleData.sampleInputData1();
    data.add("");
    data.add("");
    data.add("");
    InputConverterComponent inputConverterComponent = new InputConverterComponent(data, "|", system, 1);
    inputConverterComponent.run().get();

    ArrayList<String> expected = SampleData.sampleOutputData1();

    String[] actual = outputStream.toString().split(System.lineSeparator());

    assertEquals(actual.length, expected.size());

    for (int i = 0; i < actual.length; i++) {
      assertEquals(actual[i].trim(), expected.get(i));
    }
  }

  @Test
  public void failEmptyInput() {
    ArrayList<String> input = new ArrayList<>();

    InputConverterComponent inputConverterComponent = new InputConverterComponent(input, "|", system, 1);

    ExecutionException thrown = assertThrows(ExecutionException.class, () -> inputConverterComponent.run().get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: Found unexpected empty group of CSV");
  }

  @Test
  public void failInvalidRootLine() {
    ArrayList<String> input = new ArrayList<>();
    input.add("T|0768-101801|08-101801");

    InputConverterComponent inputConverterComponent = new InputConverterComponent(input, "|", system, 1);

    Exception thrown = assertThrows(Exception.class, () -> inputConverterComponent.run().get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: Group must start with a line that has prefix [P], found [T]");
  }

  @Test
  public void failUnsupportedPrefix() {
    ArrayList<String> input = new ArrayList<>();
    input.add("P|Carl Gustaf|Bernadotte");
    input.add("K|a|b|c");

    InputConverterComponent inputConverterComponent = new InputConverterComponent(input, "|", system, 1);

    ExecutionException thrown = assertThrows(ExecutionException.class, () -> inputConverterComponent.run().get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: Unsupported prefix [K]");
  }
}
