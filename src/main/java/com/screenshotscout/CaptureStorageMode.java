package com.screenshotscout;

/** An open string-backed screenshot storage mode. */
public final class CaptureStorageMode extends CaptureStringValue {
  public static final CaptureStorageMode EXTERNAL = new CaptureStorageMode("external");
  public static final CaptureStorageMode MANAGED = new CaptureStorageMode("managed");

  private CaptureStorageMode(String value) {
    super(value);
  }

  /** Creates a storage mode from an arbitrary raw service value. */
  public static CaptureStorageMode fromString(String value) {
    return new CaptureStorageMode(value);
  }
}
