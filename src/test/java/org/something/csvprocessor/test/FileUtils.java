package org.something.csvprocessor.test;

import org.something.csvprocessor.processing.FileLineIterator;

import java.io.*;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class FileUtils {
  public static void writeFile(File file, ArrayList<String> data) throws IOException {
    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

    for (String s : data) {
      bufferedWriter.write(s);
      bufferedWriter.newLine();
    }

    bufferedWriter.close();
  }

  public static void verifyFile(ArrayList<String> expected, File destinationFile) throws FileNotFoundException {
    ArrayList<String> actual = new ArrayList<>();
    new FileLineIterator(destinationFile).forEachRemaining(actual::add);

    assertEquals(actual.size(), expected.size());

    for (int i = 0; i < actual.size(); i++) {
      assertEquals(actual.get(i).trim(), expected.get(i));
    }
  }
}
