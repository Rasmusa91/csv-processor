package org.something.csvprocessor.component;

import java.util.concurrent.CompletableFuture;

/**
 * Interface describing a runnable component
 */
public interface Component {
  CompletableFuture<Void> run();
  CompletableFuture<Void> stop();
}
