package com.screenshotscout;

/** An open string-backed CSS media type. */
public final class CaptureMediaType extends CaptureStringValue {
  public static final CaptureMediaType PRINT = new CaptureMediaType("print");
  public static final CaptureMediaType SCREEN = new CaptureMediaType("screen");

  private CaptureMediaType(String value) {
    super(value);
  }

  /** Creates a media type from an arbitrary raw service value. */
  public static CaptureMediaType fromString(String value) {
    return new CaptureMediaType(value);
  }
}
