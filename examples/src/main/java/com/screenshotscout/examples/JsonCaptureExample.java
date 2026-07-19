package com.screenshotscout.examples;

import com.screenshotscout.CaptureOptions;
import com.screenshotscout.CaptureResponseType;
import com.screenshotscout.JsonCaptureResponse;
import com.screenshotscout.ScreenshotScoutClient;
import com.screenshotscout.ScreenshotScoutClientBuilder;

public final class JsonCaptureExample {
  private JsonCaptureExample() {}

  public static void main(String[] args) throws Exception {
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

    CaptureOptions options =
        CaptureOptions.builder()
            .responseType(CaptureResponseType.JSON)
            .build();
    JsonCaptureResponse response =
        (JsonCaptureResponse) client.capture("https://example.com", options);
    response.result().screenshotUrl().ifPresent(System.out::println);
  }
}
