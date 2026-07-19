package com.screenshotscout;

/** An open string-backed screenshot or PDF output format. */
public final class CaptureFormat extends CaptureStringValue {
  public static final CaptureFormat GIF = new CaptureFormat("gif");
  public static final CaptureFormat JPEG = new CaptureFormat("jpeg");
  public static final CaptureFormat JPG = new CaptureFormat("jpg");
  public static final CaptureFormat PDF = new CaptureFormat("pdf");
  public static final CaptureFormat PNG = new CaptureFormat("png");
  public static final CaptureFormat TIFF = new CaptureFormat("tiff");
  public static final CaptureFormat WEBP = new CaptureFormat("webp");

  private CaptureFormat(String value) {
    super(value);
  }

  /** Creates a format from an arbitrary raw service value. */
  public static CaptureFormat fromString(String value) {
    return new CaptureFormat(value);
  }
}
