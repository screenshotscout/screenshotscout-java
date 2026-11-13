package com.screenshotscout;

import java.net.http.HttpClient;

record ClientConfiguration(
    String accessKey,
    String secretKey,
    boolean secretKeyFromEnvironment,
    String captureEndpoint,
    boolean baseUrlFromEnvironment,
    HttpClient httpClient) {
  /** Describes the configuration without revealing either key or the capture endpoint. */
  @Override
  public String toString() {
    return "ClientConfiguration[accessKey=<redacted>, secretKey="
        + (secretKey == null ? "null" : "<redacted>")
        + ", secretKeyFromEnvironment="
        + secretKeyFromEnvironment
        + ", httpClient="
        + httpClient
        + "]";
  }
}
