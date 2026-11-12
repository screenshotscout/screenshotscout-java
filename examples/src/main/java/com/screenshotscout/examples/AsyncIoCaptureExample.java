package com.screenshotscout.examples;

import com.screenshotscout.BinaryCaptureResponse;
import com.screenshotscout.ScreenshotScoutAsyncClient;

/** Demonstrates async I/O that still waits for the final inline capture response. */
public final class AsyncIoCaptureExample {
  private AsyncIoCaptureExample() {}

  public static void main(String[] args) {
    // Reads SCREENSHOTSCOUT_ACCESS_KEY
    ScreenshotScoutAsyncClient client = ScreenshotScoutAsyncClient.fromEnv();

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
