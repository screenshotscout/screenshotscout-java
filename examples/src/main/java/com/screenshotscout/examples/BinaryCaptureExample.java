package com.screenshotscout.examples;

import com.screenshotscout.BinaryCaptureResponse;
import com.screenshotscout.CaptureOptions;
import com.screenshotscout.CaptureResponse;
import com.screenshotscout.ScreenshotScoutClient;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BinaryCaptureExample {
  private BinaryCaptureExample() {}

  public static void main(String[] args) throws Exception {
    // Reads SCREENSHOTSCOUT_ACCESS_KEY
    ScreenshotScoutClient client = ScreenshotScoutClient.fromEnv();

    CaptureResponse response =
        client.capture(
            "https://example.com", CaptureOptions.builder().fullPage(true).build());
    BinaryCaptureResponse binary = (BinaryCaptureResponse) response;
    Files.write(Path.of("screenshot.png"), binary.bytes());
    binary.screenshotUrl().ifPresent(System.out::println);
  }
}
