package com.screenshotscout;

/** Base class for Screenshot Scout SDK failures. */
public class ScreenshotScoutException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ScreenshotScoutException(String message) {
    super(message);
  }

  public ScreenshotScoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
