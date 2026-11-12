package com.screenshotscout;

import java.net.http.HttpClient;

record ClientConfiguration(
    String accessKey, String secretKey, boolean secretKeyFromEnvironment, HttpClient httpClient) {
  /** Describes the configuration without revealing either key. */
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
