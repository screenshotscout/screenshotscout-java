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
    String accessKey = System.getenv("SCREENSHOTSCOUT_ACCESS_KEY");
    if (accessKey == null || accessKey.isBlank()) {
      throw new IllegalStateException("Set SCREENSHOTSCOUT_ACCESS_KEY first.");
    }
    ScreenshotScoutClient client = ScreenshotScoutClient.builder().accessKey(accessKey).build();

    CaptureResponse response =
        client.capture(
            "https://example.com", CaptureOptions.builder().fullPage(true).build());
    BinaryCaptureResponse binary = (BinaryCaptureResponse) response;
    Files.write(Path.of("screenshot.png"), binary.bytes());
    binary.screenshotUrl().ifPresent(System.out::println);
  }
}
