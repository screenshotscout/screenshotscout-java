package com.screenshotscout;

/** An open string-backed successful-response representation. */
public final class CaptureResponseType extends CaptureStringValue {
  public static final CaptureResponseType BINARY = new CaptureResponseType("binary");
  public static final CaptureResponseType JSON = new CaptureResponseType("json");

  private CaptureResponseType(String value) {
    super(value);
  }

  /** Creates a response type from an arbitrary raw service value. */
  public static CaptureResponseType fromString(String value) {
    return new CaptureResponseType(value);
  }
}
