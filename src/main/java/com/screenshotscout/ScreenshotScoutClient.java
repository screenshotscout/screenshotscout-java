package com.screenshotscout;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.function.Function;

/** Reusable, thread-safe blocking Screenshot Scout client. */
public final class ScreenshotScoutClient {
  private final ScreenshotScoutClientSupport support;

  ScreenshotScoutClient(ClientConfiguration configuration) {
    support = new ScreenshotScoutClientSupport(configuration);
  }

  public static ScreenshotScoutClientBuilder builder() {
    return new ScreenshotScoutClientBuilder();
  }

  /**
   * Creates a client with the access key from {@code SCREENSHOTSCOUT_ACCESS_KEY} and, if they're
   * set, the secret key from {@code SCREENSHOTSCOUT_SECRET_KEY} and the base URL from {@code
   * SCREENSHOTSCOUT_BASE_URL}. Same as {@code builder().fromEnv().build()}.
   *
   * @throws ScreenshotScoutConfigurationException if {@code SCREENSHOTSCOUT_ACCESS_KEY} isn't set,
   *     either variable holds a blank or invalid key, or {@code SCREENSHOTSCOUT_BASE_URL} is set
   *     but blank
   */
  public static ScreenshotScoutClient fromEnv() {
    return fromEnv(System::getenv);
  }

  static ScreenshotScoutClient fromEnv(Function<String, String> environment) {
    return builder().environment(environment).fromEnv().build();
  }

  /**
   * Sends one POST request, or one signed GET request when this client has a secret key, and waits
   * for the final inline capture response.
   *
   * @throws InterruptedException if the waiting thread is interrupted; its interrupted status is
   *     preserved
   */
  public CaptureResponse capture(String url) throws InterruptedException {
    return capture(url, null);
  }

  /**
   * Sends one POST request, or one signed GET request when this client has a secret key, and waits
   * for the final inline capture response.
   *
   * @throws InterruptedException if the waiting thread is interrupted; its interrupted status is
   *     preserved
   */
  public CaptureResponse capture(String url, CaptureOptions options) throws InterruptedException {
    return send(support.prepare(url, options));
  }

  /**
   * Sends one GET or POST request and waits for the final inline capture response. A client with a
   * secret key signs GET requests and refuses POST requests, which can't be signed.
   *
   * @throws ScreenshotScoutConfigurationException if {@code method} is POST and this client has a
   *     secret key
   * @throws InterruptedException if the waiting thread is interrupted; its interrupted status is
   *     preserved
   */
  public CaptureResponse capture(String url, CaptureOptions options, CaptureHttpMethod method)
      throws InterruptedException {
    return send(support.prepare(url, options, method));
  }

  private CaptureResponse send(PreparedCapture prepared) throws InterruptedException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException("The Screenshot Scout capture call was interrupted.");
    }

    try {
      HttpResponse<byte[]> response =
          support.httpClient().send(prepared.request(), HttpResponse.BodyHandlers.ofByteArray());
      return support.decode(response, prepared.expectedKind());
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw interrupted;
    } catch (IOException | RuntimeException cause) {
      if (cause instanceof ScreenshotScoutException sdkException) {
        throw sdkException;
      }
      throw support.createTransportException(cause);
    }
  }

  /** Builds a sensitive GET capture URL without performing network I/O. */
  public String buildCaptureUrl(String url) {
    return buildCaptureUrl(url, null);
  }

  /** Builds a sensitive GET capture URL without performing network I/O. */
  public String buildCaptureUrl(String url, CaptureOptions options) {
    return support.buildCaptureUrl(url, options);
  }
}
