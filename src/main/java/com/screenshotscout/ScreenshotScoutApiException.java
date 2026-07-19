package com.screenshotscout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Indicates a non-successful HTTP response from the Screenshot Scout API. */
public final class ScreenshotScoutApiException extends ScreenshotScoutException {
  private static final long serialVersionUID = 1L;

  private final RawResponse rawResponse;
  private final boolean hasResponseBody;
  private final Object responseBody;
  private final String errorCode;
  private final String errorMessage;
  private final List<Object> errors;

  ScreenshotScoutApiException(
      RawResponse rawResponse,
      boolean hasResponseBody,
      Object responseBody,
      String errorCode,
      String errorMessage,
      List<Object> errors) {
    super(
        errorMessage != null
            ? errorMessage
            : "Screenshot Scout API request failed with status " + rawResponse.statusCode() + ".");
    this.rawResponse = rawResponse;
    this.hasResponseBody = hasResponseBody;
    this.responseBody = responseBody;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.errors = errors == null ? null : Collections.unmodifiableList(new ArrayList<>(errors));
  }

  public int statusCode() {
    return rawResponse.statusCode();
  }

  public Optional<String> errorCode() {
    return Optional.ofNullable(errorCode);
  }

  public Optional<String> errorMessage() {
    return Optional.ofNullable(errorMessage);
  }

  /** Returns whether the body was valid JSON, including a valid JSON {@code null}. */
  public boolean hasResponseBody() {
    return hasResponseBody;
  }

  /** Returns the decoded JSON value, or {@code null} for absent/unparseable JSON or JSON null. */
  public Object responseBody() {
    return responseBody;
  }

  public Optional<List<Object>> errors() {
    return Optional.ofNullable(errors);
  }

  public RawResponse rawResponse() {
    return rawResponse;
  }
}
