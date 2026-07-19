package com.screenshotscout;

import java.util.Optional;

final class DefaultBinaryCaptureResponse implements BinaryCaptureResponse {
  private final byte[] bytes;
  private final String screenshotUrl;
  private final String screenshotUrlExpiresAt;
  private final String cacheStatus;
  private final RawResponse rawResponse;

  DefaultBinaryCaptureResponse(
      RawResponse rawResponse,
      String screenshotUrl,
      String screenshotUrlExpiresAt,
      String cacheStatus) {
    this.rawResponse = rawResponse;
    this.bytes = rawResponse.bodyUnsafe();
    this.screenshotUrl = screenshotUrl;
    this.screenshotUrlExpiresAt = screenshotUrlExpiresAt;
    this.cacheStatus = cacheStatus;
  }

  @Override
  public byte[] bytes() {
    return bytes.clone();
  }

  @Override
  public Optional<String> screenshotUrl() {
    return Optional.ofNullable(screenshotUrl);
  }

  @Override
  public Optional<String> screenshotUrlExpiresAt() {
    return Optional.ofNullable(screenshotUrlExpiresAt);
  }

  @Override
  public Optional<String> cacheStatus() {
    return Optional.ofNullable(cacheStatus);
  }

  @Override
  public RawResponse rawResponse() {
    return rawResponse;
  }
}
