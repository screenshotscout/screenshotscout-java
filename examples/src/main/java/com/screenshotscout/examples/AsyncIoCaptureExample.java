package com.screenshotscout.examples;

import com.screenshotscout.BinaryCaptureResponse;
import com.screenshotscout.ScreenshotScoutAsyncClient;

/** Demonstrates async I/O that still waits for the final inline capture response. */
public final class AsyncIoCaptureExample {
  private AsyncIoCaptureExample() {}

  public static void main(String[] args) {
    String accessKey = System.getenv("SCREENSHOTSCOUT_ACCESS_KEY");
    if (accessKey == null || accessKey.isBlank()) {
      throw new IllegalStateException("Set SCREENSHOTSCOUT_ACCESS_KEY first.");
    }
    ScreenshotScoutAsyncClient client =
        ScreenshotScoutAsyncClient.builder().accessKey(accessKey).buildAsync();

    client
        .capture("https://example.com")
        .thenAccept(
            response -> {
              BinaryCaptureResponse binary = (BinaryCaptureResponse) response;
              System.out.println("Received " + binary.bytes().length + " bytes.");
            })
        .join();
  }
}
