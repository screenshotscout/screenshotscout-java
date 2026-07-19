package com.screenshotscout;

import java.io.IOException;
import java.net.http.HttpResponse;

/** Reusable blocking Screenshot Scout client. */
public final class ScreenshotScoutClient {
  private final ScreenshotScoutClientSupport support;

  ScreenshotScoutClient(ClientConfiguration configuration) {
    support = new ScreenshotScoutClientSupport(configuration);
  }

  public static ScreenshotScoutClientBuilder builder() {
    return ScreenshotScoutClientBuilder.builder();
  }

  /** Sends one POST request and waits for the final inline capture response. */
  public CaptureResponse capture(String url) throws InterruptedException {
    return capture(url, null, CaptureHttpMethod.POST);
  }

  /** Sends one POST request and waits for the final inline capture response. */
  public CaptureResponse capture(String url, CaptureOptions options) throws InterruptedException {
    return capture(url, options, CaptureHttpMethod.POST);
  }

  /** Sends one GET or POST request and waits for the final inline capture response. */
  public CaptureResponse capture(String url, CaptureOptions options, CaptureHttpMethod method)
      throws InterruptedException {
    PreparedCapture prepared = support.prepare(url, options, method);
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
      throw new ScreenshotScoutTransportException(
          "Screenshot Scout request failed before a complete HTTP response body was received.",
          cause);
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
