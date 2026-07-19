package com.screenshotscout;

/** Indicates a successful response that could not be decoded as requested. */
@SuppressWarnings("serial") // Raw response payloads intentionally retain their runtime types.
public final class ScreenshotScoutResponseDecodingException extends ScreenshotScoutException {
  private static final long serialVersionUID = 1L;

  private final RawResponse rawResponse;

  public ScreenshotScoutResponseDecodingException(
      String message, RawResponse rawResponse, Throwable cause) {
    super(message, cause);
    this.rawResponse = rawResponse;
  }

  public RawResponse rawResponse() {
    return rawResponse;
  }
}
