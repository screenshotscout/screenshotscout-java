package com.screenshotscout.examples;

import com.screenshotscout.CaptureOptions;
import com.screenshotscout.ScreenshotScoutClient;
import com.screenshotscout.ScreenshotScoutClientBuilder;

public final class BuildCaptureUrlExample {
  private BuildCaptureUrlExample() {}

  public static void main(String[] args) {
    String accessKey = System.getenv("SCREENSHOTSCOUT_ACCESS_KEY");
    if (accessKey == null || accessKey.isBlank()) {
      throw new IllegalStateException("Set SCREENSHOTSCOUT_ACCESS_KEY first.");
    }
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder().accessKey(accessKey);
    String secretKey = System.getenv("SCREENSHOTSCOUT_SECRET_KEY");
    if (secretKey != null && !secretKey.isBlank()) {
      builder.secretKey(secretKey);
    }
    ScreenshotScoutClient client = builder.build();

    String url =
        client.buildCaptureUrl(
            "https://example.com",
            CaptureOptions.builder().fullPage(true).blockAds(true).build());
    System.out.println(url);
  }
}
