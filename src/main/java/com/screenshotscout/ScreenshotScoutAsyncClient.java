package com.screenshotscout;

import java.net.http.HttpResponse;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

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

  /**
   * Creates a client with the access key from {@code SCREENSHOTSCOUT_ACCESS_KEY} and, if it's set,
   * the secret key from {@code SCREENSHOTSCOUT_SECRET_KEY}. Same as {@code
   * builder().fromEnv().buildAsync()}.
   *
   * @throws ScreenshotScoutConfigurationException if {@code SCREENSHOTSCOUT_ACCESS_KEY} isn't set,
   *     or either variable holds a blank or invalid key
   */
  public static ScreenshotScoutAsyncClient fromEnv() {
    return fromEnv(System::getenv);
  }

  static ScreenshotScoutAsyncClient fromEnv(Function<String, String> environment) {
    return builder().environment(environment).fromEnv().buildAsync();
  }

  /**
   * Starts one POST call, or one signed GET call when this client has a secret key, whose future
   * completes with the final inline capture response.
   */
  public CompletableFuture<CaptureResponse> capture(String url) {
    return capture(url, null);
  }

  /**
   * Starts one POST call, or one signed GET call when this client has a secret key, whose future
   * completes with the final inline capture response.
   */
  public CompletableFuture<CaptureResponse> capture(String url, CaptureOptions options) {
    return send(support.prepare(url, options));
  }

  /**
   * Starts one GET or POST call whose future completes with the final inline response. A client
   * with a secret key signs GET requests and refuses POST requests, which can't be signed.
   *
   * @throws ScreenshotScoutConfigurationException if {@code method} is POST and this client has a
   *     secret key
   */
  public CompletableFuture<CaptureResponse> capture(
      String url, CaptureOptions options, CaptureHttpMethod method) {
    return send(support.prepare(url, options, method));
  }

  private CompletableFuture<CaptureResponse> send(PreparedCapture prepared) {
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
