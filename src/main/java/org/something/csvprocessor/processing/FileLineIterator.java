package org.something.csvprocessor.processing;

import org.something.csvprocessor.monad.Try;

import java.io.*;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Convenience iterator that invokes the Java Scanner line iterator
 */
public class FileLineIterator implements Iterator<String> {
  @Override
  public boolean hasNext() {
    return scanner.hasNextLine();
  }

  @Override
  public String next() {
    return scanner.nextLine();
  }

  private final FileInputStream fileInputStream;
  private final Scanner scanner;

  public FileLineIterator(File file) throws FileNotFoundException {
    this.fileInputStream = new FileInputStream(file);
    this.scanner = new Scanner(fileInputStream);
  }

  public void closeUnsafe() throws IOException {
    fileInputStream.close();
    scanner.close();
  }

  public Try<Void> close() {
    return Try.of(() -> {
      closeUnsafe();
      return null;
    });
  }
}
