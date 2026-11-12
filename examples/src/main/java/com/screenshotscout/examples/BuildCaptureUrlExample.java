package com.screenshotscout.examples;

import com.screenshotscout.CaptureOptions;
import com.screenshotscout.ScreenshotScoutClient;

public final class BuildCaptureUrlExample {
  private BuildCaptureUrlExample() {}

  public static void main(String[] args) {
    // Reads SCREENSHOTSCOUT_ACCESS_KEY, and SCREENSHOTSCOUT_SECRET_KEY if it's set
    ScreenshotScoutClient client = ScreenshotScoutClient.fromEnv();

    String url =
        client.buildCaptureUrl(
            "https://example.com",
            CaptureOptions.builder().fullPage(true).blockAds(true).build());
    System.out.println(url);
  }
}
