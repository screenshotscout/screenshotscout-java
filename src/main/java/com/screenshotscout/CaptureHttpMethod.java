package com.screenshotscout;

/** The closed HTTP method selector for one capture call. */
public enum CaptureHttpMethod {
  /** Send capture options as query parameters. */
  GET,

  /** Send capture options as a JSON request body. */
  POST
}
