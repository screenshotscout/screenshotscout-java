package com.screenshotscout;

/** An open string-backed image resizing anchor. */
public final class CaptureImageAnchor extends CaptureStringValue {
  public static final CaptureImageAnchor BOTTOM = new CaptureImageAnchor("bottom");
  public static final CaptureImageAnchor BOTTOM_LEFT = new CaptureImageAnchor("bottom_left");
  public static final CaptureImageAnchor BOTTOM_RIGHT = new CaptureImageAnchor("bottom_right");
  public static final CaptureImageAnchor CENTER = new CaptureImageAnchor("center");
  public static final CaptureImageAnchor LEFT = new CaptureImageAnchor("left");
  public static final CaptureImageAnchor RIGHT = new CaptureImageAnchor("right");
  public static final CaptureImageAnchor TOP = new CaptureImageAnchor("top");
  public static final CaptureImageAnchor TOP_LEFT = new CaptureImageAnchor("top_left");
  public static final CaptureImageAnchor TOP_RIGHT = new CaptureImageAnchor("top_right");

  private CaptureImageAnchor(String value) {
    super(value);
  }

  /** Creates an image anchor from an arbitrary raw service value. */
  public static CaptureImageAnchor fromString(String value) {
    return new CaptureImageAnchor(value);
  }
}
