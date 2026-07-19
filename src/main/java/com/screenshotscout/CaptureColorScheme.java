package com.screenshotscout;

/** An open string-backed emulated color scheme. */
public final class CaptureColorScheme extends CaptureStringValue {
  public static final CaptureColorScheme AUTO = new CaptureColorScheme("auto");
  public static final CaptureColorScheme DARK = new CaptureColorScheme("dark");
  public static final CaptureColorScheme LIGHT = new CaptureColorScheme("light");

  private CaptureColorScheme(String value) {
    super(value);
  }

  /** Creates a color scheme from an arbitrary raw service value. */
  public static CaptureColorScheme fromString(String value) {
    return new CaptureColorScheme(value);
  }
}
