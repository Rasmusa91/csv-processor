package org.something.csvprocessor.monad;

import org.something.csvprocessor.function.ThrowingSupplier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Convenience monad to suppress exceptions
 */
public interface Try<T> {
  static <U> Try<U> of(ThrowingSupplier<U> s) {
    try {
      return Try.success(s.get());
    } catch (Throwable e) {
      return Try.failure(e);
    }
  }

  static <U> Try<U> success(U value) {
    return new Success<>(value);
  }

  static <U> Try<U> failure(Throwable e) {
    return new Failure<>(e);
  }

  boolean isFailure();

  T get() throws Throwable;

  <U> Try<U> map(Function<T, U> f);

  <U> Try<U> flatMap(Function<T, Try<U>> f);

  boolean exists(Predicate<T> p);

  CompletableFuture<T> toCompletableFuture();

  Optional<T> toOptional();

  void logError();
}

class Success<T> implements Try<T> {
  private final T value;

  public Success(T value) {
    this.value = value;
  }

  @Override
  public boolean isFailure() {
    return false;
  }

  @Override
  public T get() {
    return value;
  }

  @Override
  public <U> Try<U> map(Function<T, U> f) {
    return Try.success(f.apply(value));
  }

  @Override
  public <U> Try<U> flatMap(Function<T, Try<U>> f) {
    return f.apply(value);
  }

  @Override
  public boolean exists(Predicate<T> p) {
    return p.test(value);
  }

  @Override
  public CompletableFuture<T> toCompletableFuture() {
    return CompletableFuture.completedFuture(value);
  }

  @Override
  public Optional<T> toOptional() {
    return Optional.ofNullable(value);
  }

  @Override
  public void logError() {}
}

class Failure<T> implements Try<T> {
  private final Throwable exception;

  Failure(Throwable exception) {
    this.exception = exception;
  }

  @Override
  public boolean isFailure() {
    return true;
  }

  @Override
  public T get() throws Throwable {
    throw exception;
  }

  @Override
  public <U> Try<U> map(Function<T, U> f) {
    return Try.failure(exception);
  }

  @Override
  public <U> Try<U> flatMap(Function<T, Try<U>> f) {
    return Try.failure(exception);
  }

  @Override
  public boolean exists(Predicate<T> p) {
    return false;
  }

  @Override
  public CompletableFuture<T> toCompletableFuture() {
    return CompletableFuture.failedFuture(exception);
  }

  @Override
  public Optional<T> toOptional() {
    return Optional.empty();
  }

  @Override
  public void logError() {
    System.err.print(exception.getMessage());
  }
}