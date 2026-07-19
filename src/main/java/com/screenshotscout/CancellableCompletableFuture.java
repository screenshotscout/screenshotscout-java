package com.screenshotscout;

import java.util.concurrent.CompletableFuture;

/** A public-facing future whose cancellation is forwarded to the JDK transport future. */
final class CancellableCompletableFuture<T> extends CompletableFuture<T> {
  private final CompletableFuture<?> transportFuture;

  CancellableCompletableFuture(CompletableFuture<?> transportFuture) {
    this.transportFuture = transportFuture;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    boolean cancelled = super.cancel(mayInterruptIfRunning);
    if (cancelled) {
      transportFuture.cancel(mayInterruptIfRunning);
    }
    return cancelled;
  }
}
