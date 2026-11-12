package com.screenshotscout;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.function.Function;
import java.util.regex.Pattern;

/** Builds reusable blocking and async-I/O Screenshot Scout clients. */
public final class ScreenshotScoutClientBuilder {
  static final String ACCESS_KEY_VARIABLE = "SCREENSHOTSCOUT_ACCESS_KEY";
  static final String SECRET_KEY_VARIABLE = "SCREENSHOTSCOUT_SECRET_KEY";

  private static final Pattern ACCESS_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9\\-._~+/]+=*$");

  private String accessKey;
  private String secretKey;
  private boolean fromEnv;
  private Function<String, String> environment = System::getenv;
  private HttpClient httpClient;

  ScreenshotScoutClientBuilder() {}

  /** Sets the Screenshot Scout access key, which is required unless you use {@link #fromEnv()}. */
  public ScreenshotScoutClientBuilder accessKey(String value) {
    accessKey = value;
    return this;
  }

  /**
   * Sets the optional secret key, which signs capture requests and capture URLs. A non-null value
   * must be non-blank. A client with a secret key sends captures as GET by default and refuses POST
   * requests, which can't be signed.
   */
  public ScreenshotScoutClientBuilder secretKey(String value) {
    secretKey = value;
    return this;
  }

  /**
   * Reads the keys from environment variables when the client is built: the access key from {@code
   * SCREENSHOTSCOUT_ACCESS_KEY} and, if it's set, the secret key from {@code
   * SCREENSHOTSCOUT_SECRET_KEY}. Can't be combined with {@link #accessKey(String)} or {@link
   * #secretKey(String)}.
   */
  public ScreenshotScoutClientBuilder fromEnv() {
    fromEnv = true;
    return this;
  }

  /** Replaces the environment variable lookup that {@link #fromEnv()} uses. */
  ScreenshotScoutClientBuilder environment(Function<String, String> lookup) {
    environment = lookup;
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
    String selectedAccessKey = accessKey;
    String selectedSecretKey = secretKey;
    if (fromEnv) {
      if (accessKey != null || secretKey != null) {
        throw new ScreenshotScoutConfigurationException(
            "fromEnv() can't be combined with accessKey() or secretKey().");
      }
      selectedAccessKey = readVariable(ACCESS_KEY_VARIABLE);
      selectedSecretKey = readVariable(SECRET_KEY_VARIABLE);
      if (selectedAccessKey == null) {
        throw new ScreenshotScoutConfigurationException(
            "A non-blank Screenshot Scout access key is required. Pass one, or set "
                + ACCESS_KEY_VARIABLE
                + ".");
      }
    }

    if (selectedAccessKey == null || selectedAccessKey.trim().isEmpty()) {
      throw new ScreenshotScoutConfigurationException(
          "A non-blank Screenshot Scout access key is required.");
    }
    if (!ACCESS_KEY_PATTERN.matcher(selectedAccessKey).matches()) {
      throw new ScreenshotScoutConfigurationException(
          "The Screenshot Scout access key is not a valid Bearer credential.");
    }
    if (selectedSecretKey != null && selectedSecretKey.trim().isEmpty()) {
      throw new ScreenshotScoutConfigurationException(
          "The Screenshot Scout secret key must be non-blank when provided.");
    }

    try {
      HttpRequest.newBuilder(ScreenshotScoutClientSupport.CAPTURE_ENDPOINT)
          .header("Authorization", "Bearer " + selectedAccessKey);
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
    return new ClientConfiguration(
        selectedAccessKey, selectedSecretKey, fromEnv && selectedSecretKey != null, selectedClient);
  }

  /** Returns the variable's value, or null when it's unset or empty. */
  private String readVariable(String name) {
    String value = environment.apply(name);
    if (value == null || value.isEmpty()) {
      return null;
    }
    if (value.trim().isEmpty()) {
      throw new ScreenshotScoutConfigurationException(name + " is set but blank.");
    }
    return value;
  }
}
