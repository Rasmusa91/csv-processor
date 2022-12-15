package org.something.csvprocessor.test.processing;

import org.something.csvprocessor.processing.FileLineIterator;
import org.something.csvprocessor.test.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class FileLineIteratorTest {
  @Test
  public void shouldReadLineByLineSuccessfully() throws IOException {
    ArrayList<String> expected = new ArrayList<>();
    expected.add("1");
    expected.add("2");
    expected.add("3");
    expected.add("4");
    expected.add("5");

    File file = Files.createTempFile("tmp", "tmp").toFile();
    FileUtils.writeFile(file, expected);

    ArrayList<String> actual = new ArrayList<>();
    FileLineIterator fileLineIterator = new FileLineIterator(file);
    fileLineIterator.forEachRemaining(actual::add);

    assertEquals(expected.size(), actual.size());

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actual.get(i));
    }
  }
}
