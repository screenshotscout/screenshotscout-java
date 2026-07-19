package com.screenshotscout;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

final class EcmaScriptNumberFormatter {
  private static final int MAX_SIGNIFICANT_DIGITS = 17;

  private EcmaScriptNumberFormatter() {}

  static String format(double value) {
    if (!Double.isFinite(value)) {
      throw new IllegalArgumentException("Expected a finite double value.");
    }
    if (value == 0) {
      return "0";
    }

    boolean negative = value < 0;
    double absoluteValue = Math.abs(value);
    BigDecimal exactValue = new BigDecimal(absoluteValue);
    int decimalPosition = exactValue.precision() - exactValue.scale();
    long expectedBits = Double.doubleToRawLongBits(absoluteValue);

    for (int significantDigits = 1;
        significantDigits <= MAX_SIGNIFICANT_DIGITS;
        significantDigits++) {
      int power = decimalPosition - significantDigits;
      BigDecimal unit = BigDecimal.ONE.scaleByPowerOfTen(power);
      BigInteger lowerSignificand =
          exactValue.divide(unit, 0, RoundingMode.FLOOR).toBigIntegerExact();

      BigDecimal selected = null;
      BigDecimal selectedDistance = null;
      for (BigInteger significand :
          new BigInteger[] {lowerSignificand, lowerSignificand.add(BigInteger.ONE)}) {
        BigDecimal candidate = new BigDecimal(significand).scaleByPowerOfTen(power);
        if (!roundTrips(candidate, expectedBits)) {
          continue;
        }

        BigDecimal distance = exactValue.subtract(candidate).abs();
        if (selected == null
            || distance.compareTo(selectedDistance) < 0
            || (distance.compareTo(selectedDistance) == 0
                && hasEvenSignificand(candidate)
                && !hasEvenSignificand(selected))) {
          selected = candidate;
          selectedDistance = distance;
        }
      }

      if (selected != null) {
        String formatted = formatDecimal(selected);
        return negative ? "-" + formatted : formatted;
      }
    }

    throw new IllegalStateException("A finite double did not have a round-tripping decimal value.");
  }

  private static boolean roundTrips(BigDecimal candidate, long expectedBits) {
    double parsed = Double.parseDouble(candidate.toString());
    return Double.doubleToRawLongBits(parsed) == expectedBits;
  }

  private static boolean hasEvenSignificand(BigDecimal value) {
    String digits = value.stripTrailingZeros().unscaledValue().abs().toString();
    return (digits.charAt(digits.length() - 1) - '0') % 2 == 0;
  }

  private static String formatDecimal(BigDecimal value) {
    BigDecimal normalized = value.stripTrailingZeros();
    String digits = normalized.unscaledValue().abs().toString();
    int digitCount = digits.length();
    int decimalPosition = digitCount - normalized.scale();

    if (digitCount <= decimalPosition && decimalPosition <= 21) {
      return digits + "0".repeat(decimalPosition - digitCount);
    }
    if (0 < decimalPosition && decimalPosition <= 21) {
      return digits.substring(0, decimalPosition) + "." + digits.substring(decimalPosition);
    }
    if (-6 < decimalPosition && decimalPosition <= 0) {
      return "0." + "0".repeat(-decimalPosition) + digits;
    }

    int exponent = decimalPosition - 1;
    String mantissa = digitCount == 1 ? digits : digits.charAt(0) + "." + digits.substring(1);
    return mantissa + "e" + (exponent >= 0 ? "+" : "") + exponent;
  }
}
