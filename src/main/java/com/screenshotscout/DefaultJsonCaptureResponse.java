package com.screenshotscout;

import java.util.Objects;

final class DefaultJsonCaptureResponse implements JsonCaptureResponse {
  private final CaptureResult result;
  private final RawResponse rawResponse;

  DefaultJsonCaptureResponse(CaptureResult result, RawResponse rawResponse) {
    this.result = Objects.requireNonNull(result, "result");
    this.rawResponse = Objects.requireNonNull(rawResponse, "rawResponse");
  }

  @Override
  public CaptureResult result() {
    return result;
  }

  @Override
  public RawResponse rawResponse() {
    return rawResponse;
  }
}
