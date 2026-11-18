package com.screenshotscout;

import java.util.Optional;

final class DefaultBinaryCaptureResponse implements BinaryCaptureResponse {
  private final byte[] bytes;
  private final String screenshotUrl;
  private final String screenshotUrlExpiresAt;
  private final String cacheStatus;
  private final String markdown;
  private final String html;
  private final RawResponse rawResponse;

  DefaultBinaryCaptureResponse(
      RawResponse rawResponse,
      String screenshotUrl,
      String screenshotUrlExpiresAt,
      String cacheStatus,
      String markdown,
      String html) {
    this.rawResponse = rawResponse;
    this.bytes = rawResponse.bodyUnsafe();
    this.screenshotUrl = screenshotUrl;
    this.screenshotUrlExpiresAt = screenshotUrlExpiresAt;
    this.cacheStatus = cacheStatus;
    this.markdown = markdown;
    this.html = html;
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
  public Optional<String> markdown() {
    return Optional.ofNullable(markdown);
  }

  @Override
  public Optional<String> html() {
    return Optional.ofNullable(html);
  }

  @Override
  public RawResponse rawResponse() {
    return rawResponse;
  }
}
