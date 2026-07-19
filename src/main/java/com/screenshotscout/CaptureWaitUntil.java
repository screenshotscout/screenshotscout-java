package com.screenshotscout;

/** An open string-backed page-navigation completion condition. */
public final class CaptureWaitUntil extends CaptureStringValue {
  public static final CaptureWaitUntil DOM_CONTENT_LOADED =
      new CaptureWaitUntil("domcontentloaded");
  public static final CaptureWaitUntil LOAD = new CaptureWaitUntil("load");
  public static final CaptureWaitUntil NETWORK_IDLE_0 = new CaptureWaitUntil("networkidle0");
  public static final CaptureWaitUntil NETWORK_IDLE_2 = new CaptureWaitUntil("networkidle2");

  private CaptureWaitUntil(String value) {
    super(value);
  }

  /** Creates a navigation condition from an arbitrary raw service value. */
  public static CaptureWaitUntil fromString(String value) {
    return new CaptureWaitUntil(value);
  }
}
