package com.screenshotscout;

import java.util.Optional;

/** Indicates a capture value that cannot be represented safely on the wire. */
public final class ScreenshotScoutSerializationException extends ScreenshotScoutException {
  private static final long serialVersionUID = 1L;

  private final String option;

  public ScreenshotScoutSerializationException(String message) {
    this(message, null, null);
  }

  public ScreenshotScoutSerializationException(String message, String option) {
    this(message, option, null);
  }

  public ScreenshotScoutSerializationException(String message, String option, Throwable cause) {
    super(message, cause);
    this.option = option;
  }

  /** Returns the offending public option name when one is known. */
  public Optional<String> option() {
    return Optional.ofNullable(option);
  }
}
