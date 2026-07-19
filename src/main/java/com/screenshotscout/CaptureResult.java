package com.screenshotscout;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Documented JSON capture metadata plus unrecognized fields retained without transformation. */
public final class CaptureResult {
  private final String screenshotUrl;
  private final String screenshotUrlExpiresAt;
  private final String cacheStatus;
  private final Map<String, Object> additionalFields;

  CaptureResult(
      String screenshotUrl,
      String screenshotUrlExpiresAt,
      String cacheStatus,
      Map<String, Object> additionalFields) {
    this.screenshotUrl = screenshotUrl;
    this.screenshotUrlExpiresAt = screenshotUrlExpiresAt;
    this.cacheStatus = cacheStatus;
    this.additionalFields = Collections.unmodifiableMap(new LinkedHashMap<>(additionalFields));
  }

  public Optional<String> screenshotUrl() {
    return Optional.ofNullable(screenshotUrl);
  }

  public Optional<String> screenshotUrlExpiresAt() {
    return Optional.ofNullable(screenshotUrlExpiresAt);
  }

  public Optional<String> cacheStatus() {
    return Optional.ofNullable(cacheStatus);
  }

  public Map<String, Object> additionalFields() {
    return additionalFields;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CaptureResult that)) {
      return false;
    }
    return Objects.equals(screenshotUrl, that.screenshotUrl)
        && Objects.equals(screenshotUrlExpiresAt, that.screenshotUrlExpiresAt)
        && Objects.equals(cacheStatus, that.cacheStatus)
        && additionalFields.equals(that.additionalFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(screenshotUrl, screenshotUrlExpiresAt, cacheStatus, additionalFields);
  }
}
