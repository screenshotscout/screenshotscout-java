package com.screenshotscout;

/** Indicates invalid reusable client configuration. */
public final class ScreenshotScoutConfigurationException extends ScreenshotScoutException {
  private static final long serialVersionUID = 1L;

  public ScreenshotScoutConfigurationException(String message) {
    super(message);
  }

  public ScreenshotScoutConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
