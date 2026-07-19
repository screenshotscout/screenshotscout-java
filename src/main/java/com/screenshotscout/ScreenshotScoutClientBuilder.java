package com.screenshotscout;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.regex.Pattern;

/** Builds reusable blocking and async-I/O Screenshot Scout clients. */
public final class ScreenshotScoutClientBuilder {
  private static final Pattern ACCESS_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9\\-._~+/]+=*$");

  private String accessKey;
  private String secretKey;
  private HttpClient httpClient;

  public ScreenshotScoutClientBuilder() {}

  public static ScreenshotScoutClientBuilder builder() {
    return new ScreenshotScoutClientBuilder();
  }

  /** Sets the required Screenshot Scout access key. */
  public ScreenshotScoutClientBuilder accessKey(String value) {
    accessKey = value;
    return this;
  }

  /** Sets the optional signing secret. A non-null value must be non-blank. */
  public ScreenshotScoutClientBuilder secretKey(String value) {
    secretKey = value;
    return this;
  }

  /**
   * Sets a caller-owned JDK HTTP client whose redirect, proxy, executor, and timeout behavior is
   * retained. The SDK never closes an injected client.
   */
  public ScreenshotScoutClientBuilder httpClient(HttpClient value) {
    httpClient = value;
    return this;
  }

  /** Builds a blocking client. */
  public ScreenshotScoutClient build() {
    return new ScreenshotScoutClient(validate());
  }

  /** Builds a CompletableFuture-based async-I/O client. */
  public ScreenshotScoutAsyncClient buildAsync() {
    return new ScreenshotScoutAsyncClient(validate());
  }

  private ClientConfiguration validate() {
    if (accessKey == null || accessKey.trim().isEmpty()) {
      throw new ScreenshotScoutConfigurationException(
          "A non-blank Screenshot Scout access key is required.");
    }
    if (!ACCESS_KEY_PATTERN.matcher(accessKey).matches()) {
      throw new ScreenshotScoutConfigurationException(
          "The Screenshot Scout access key is not a valid Bearer credential.");
    }
    if (secretKey != null && secretKey.trim().isEmpty()) {
      throw new ScreenshotScoutConfigurationException(
          "The Screenshot Scout secret key must be non-blank when provided.");
    }

    try {
      HttpRequest.newBuilder(ScreenshotScoutClientSupport.CAPTURE_ENDPOINT)
          .header("Authorization", "Bearer " + accessKey);
    } catch (IllegalArgumentException cause) {
      throw new ScreenshotScoutConfigurationException(
          "The Screenshot Scout access key cannot be used in an Authorization header.", cause);
    }

    HttpClient selectedClient = httpClient;
    if (selectedClient == null) {
      try {
        selectedClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
      } catch (RuntimeException cause) {
        throw new ScreenshotScoutConfigurationException(
            "A usable JDK HttpClient transport is required.", cause);
      }
    }
    return new ClientConfiguration(accessKey, secretKey, selectedClient);
  }
}
