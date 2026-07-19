package com.screenshotscout;

/** An open string-backed image resizing mode. */
public final class CaptureImageMode extends CaptureStringValue {
  public static final CaptureImageMode FILL = new CaptureImageMode("fill");
  public static final CaptureImageMode FIT = new CaptureImageMode("fit");
  public static final CaptureImageMode STRETCH = new CaptureImageMode("stretch");

  private CaptureImageMode(String value) {
    super(value);
  }

  /** Creates an image mode from an arbitrary raw service value. */
  public static CaptureImageMode fromString(String value) {
    return new CaptureImageMode(value);
  }
}
