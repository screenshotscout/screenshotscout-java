package com.screenshotscout;

/** An open string-backed PDF paper format. */
public final class CapturePdfPaperFormat extends CaptureStringValue {
  public static final CapturePdfPaperFormat A3 = new CapturePdfPaperFormat("a3");
  public static final CapturePdfPaperFormat A4 = new CapturePdfPaperFormat("a4");
  public static final CapturePdfPaperFormat A5 = new CapturePdfPaperFormat("a5");
  public static final CapturePdfPaperFormat A6 = new CapturePdfPaperFormat("a6");
  public static final CapturePdfPaperFormat LEGAL = new CapturePdfPaperFormat("legal");
  public static final CapturePdfPaperFormat LETTER = new CapturePdfPaperFormat("letter");
  public static final CapturePdfPaperFormat TABLOID = new CapturePdfPaperFormat("tabloid");

  private CapturePdfPaperFormat(String value) {
    super(value);
  }

  /** Creates a PDF paper format from an arbitrary raw service value. */
  public static CapturePdfPaperFormat fromString(String value) {
    return new CapturePdfPaperFormat(value);
  }
}
