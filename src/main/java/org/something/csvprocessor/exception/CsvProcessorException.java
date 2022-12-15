package org.something.csvprocessor.exception;

public class CsvProcessorException extends Exception {
  public CsvProcessorException(String message) {
    super(message);
  }

  public CsvProcessorException(String message, Throwable cause) {
    super(message, cause);
  }
}

