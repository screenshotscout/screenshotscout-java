package com.screenshotscout;

import java.util.Objects;

/** Shared implementation for open string-backed service values. */
abstract class CaptureStringValue {
  private final String value;

  CaptureStringValue(String value) {
    this.value = Objects.requireNonNull(value, "value");
  }

  /** Returns the raw service value without normalization. */
  public final String value() {
    return value;
  }

  @Override
  public final String toString() {
    return value;
  }

  @Override
  public final boolean equals(Object other) {
    return this == other
        || (other != null
            && getClass() == other.getClass()
            && value.equals(((CaptureStringValue) other).value));
  }

  @Override
  public final int hashCode() {
    return value.hashCode();
  }
}
