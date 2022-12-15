package org.something.csvprocessor.function;

/**
 * Convenience supplier that allows throwing
 */
public interface ThrowingSupplier<T> {
  T get() throws Throwable;
}
