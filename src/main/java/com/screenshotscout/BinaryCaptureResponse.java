package com.screenshotscout;

import java.util.Optional;

/** A successful capture whose body contains screenshot or PDF bytes. */
public interface BinaryCaptureResponse extends CaptureResponse {
  /** Returns a copy of the exact screenshot or PDF bytes. */
  byte[] bytes();

  Optional<String> screenshotUrl();

  Optional<String> screenshotUrlExpiresAt();

  Optional<String> cacheStatus();
}
