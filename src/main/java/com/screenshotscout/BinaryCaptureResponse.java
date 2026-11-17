package com.screenshotscout;

import java.util.Optional;

/** A successful capture whose body contains screenshot, PDF, or Markdown bytes. */
public interface BinaryCaptureResponse extends CaptureResponse {
  /** Returns a copy of the exact screenshot, PDF, or Markdown bytes. */
  byte[] bytes();

  Optional<String> screenshotUrl();

  Optional<String> screenshotUrlExpiresAt();

  Optional<String> cacheStatus();

  /** Returns the body decoded as UTF-8 text when the content type is {@code text/markdown}. */
  default Optional<String> markdown() {
    return Optional.empty();
  }
}
