package com.screenshotscout.examples;

import com.screenshotscout.CaptureOptions;
import com.screenshotscout.CaptureResponseType;
import com.screenshotscout.JsonCaptureResponse;
import com.screenshotscout.ScreenshotScoutClient;

public final class JsonCaptureExample {
  private JsonCaptureExample() {}

  public static void main(String[] args) throws Exception {
    String accessKey = System.getenv("SCREENSHOTSCOUT_ACCESS_KEY");
    if (accessKey == null || accessKey.isBlank()) {
      throw new IllegalStateException("Set SCREENSHOTSCOUT_ACCESS_KEY first.");
    }
    ScreenshotScoutClient client = ScreenshotScoutClient.builder().accessKey(accessKey).build();

    CaptureOptions options =
        CaptureOptions.builder()
            .responseType(CaptureResponseType.JSON)
            .build();
    JsonCaptureResponse response =
        (JsonCaptureResponse) client.capture("https://example.com", options);
    response.result().screenshotUrl().ifPresent(System.out::println);
  }
}
