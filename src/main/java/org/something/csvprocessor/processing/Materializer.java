package org.something.csvprocessor.processing;

import java.util.concurrent.CompletableFuture;

/**
 * Interface describing how to materialize T to U
 */
public interface Materializer<T, U> {
  CompletableFuture<Void> materialize(T u);
  CompletableFuture<U> result();
}
