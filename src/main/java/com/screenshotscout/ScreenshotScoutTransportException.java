package com.screenshotscout;

/** Indicates that no complete HTTP response body was received. */
public final class ScreenshotScoutTransportException extends ScreenshotScoutException {
  private static final long serialVersionUID = 1L;

  public ScreenshotScoutTransportException(String message, Throwable cause) {
    super(message, cause);
  }
}
