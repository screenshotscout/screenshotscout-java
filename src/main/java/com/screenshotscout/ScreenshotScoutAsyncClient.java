package com.screenshotscout;

import java.net.http.HttpResponse;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Reusable, thread-safe CompletableFuture-based async-I/O client. Each capture waits for the final
 * inline Screenshot Scout response; it does not create a background API job or webhook flow.
 */
public final class ScreenshotScoutAsyncClient {
  private final ScreenshotScoutClientSupport support;

  ScreenshotScoutAsyncClient(ClientConfiguration configuration) {
    support = new ScreenshotScoutClientSupport(configuration);
  }

  public static ScreenshotScoutClientBuilder builder() {
    return new ScreenshotScoutClientBuilder();
  }

  /** Starts one POST call whose future completes with the final inline capture response. */
  public CompletableFuture<CaptureResponse> capture(String url) {
    return capture(url, null, CaptureHttpMethod.POST);
  }

  /** Starts one POST call whose future completes with the final inline capture response. */
  public CompletableFuture<CaptureResponse> capture(String url, CaptureOptions options) {
    return capture(url, options, CaptureHttpMethod.POST);
  }

  /** Starts one GET or POST call whose future completes with the final inline response. */
  public CompletableFuture<CaptureResponse> capture(
      String url, CaptureOptions options, CaptureHttpMethod method) {
    PreparedCapture prepared = support.prepare(url, options, method);
    CompletableFuture<HttpResponse<byte[]>> transportFuture;
    try {
      transportFuture =
          support
              .httpClient()
              .sendAsync(prepared.request(), HttpResponse.BodyHandlers.ofByteArray());
    } catch (RuntimeException cause) {
      return CompletableFuture.failedFuture(
          new ScreenshotScoutTransportException(
              "Screenshot Scout request failed before a complete HTTP response body was received.",
              cause));
    }

    CancellableCompletableFuture<CaptureResponse> result =
        new CancellableCompletableFuture<>(transportFuture);
    transportFuture.whenComplete(
        (response, error) -> {
          if (result.isCancelled()) {
            return;
          }
          if (error != null) {
            Throwable cause = unwrapCompletionException(error);
            if (cause instanceof CancellationException) {
              result.cancel(false);
            } else {
              result.completeExceptionally(
                  new ScreenshotScoutTransportException(
                      "Screenshot Scout request failed before a complete HTTP response body was received.",
                      cause));
            }
            return;
          }
          try {
            result.complete(support.decode(response, prepared.expectedKind()));
          } catch (RuntimeException exception) {
            result.completeExceptionally(exception);
          }
        });
    return result;
  }

  /** Builds a sensitive GET capture URL without performing network I/O. */
  public String buildCaptureUrl(String url) {
    return buildCaptureUrl(url, null);
  }

  /** Builds a sensitive GET capture URL without performing network I/O. */
  public String buildCaptureUrl(String url, CaptureOptions options) {
    return support.buildCaptureUrl(url, options);
  }

  private static Throwable unwrapCompletionException(Throwable error) {
    Throwable current = error;
    while (current instanceof CompletionException && current.getCause() != null) {
      current = current.getCause();
    }
    return current;
  }
}
