package com.screenshotscout;

import java.net.http.HttpHeaders;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/** Exact buffered HTTP response data retained on successes and API failures. */
public final class RawResponse {
  private final int statusCode;
  private final HttpHeaders headers;
  private final String contentType;
  private final byte[] body;

  RawResponse(int statusCode, HttpHeaders headers, String contentType, byte[] body) {
    this.statusCode = statusCode;
    this.headers = Objects.requireNonNull(headers, "headers");
    this.contentType = contentType;
    this.body = Objects.requireNonNull(body, "body").clone();
  }

  public int statusCode() {
    return statusCode;
  }

  /** Returns immutable multi-value response headers with case-insensitive lookup methods. */
  public HttpHeaders headers() {
    return headers;
  }

  public Optional<String> contentType() {
    return Optional.ofNullable(contentType);
  }

  /** Returns a copy of the exact buffered response body. */
  public byte[] body() {
    return body.clone();
  }

  byte[] bodyUnsafe() {
    return body;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof RawResponse that)) {
      return false;
    }
    return statusCode == that.statusCode
        && headers.equals(that.headers)
        && Objects.equals(contentType, that.contentType)
        && Arrays.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(statusCode, headers, contentType);
    return 31 * result + Arrays.hashCode(body);
  }
}
