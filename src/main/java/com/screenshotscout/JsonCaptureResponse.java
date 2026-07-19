package com.screenshotscout;

/** A successful capture whose body contains a JSON result. */
public interface JsonCaptureResponse extends CaptureResponse {
  CaptureResult result();
}
