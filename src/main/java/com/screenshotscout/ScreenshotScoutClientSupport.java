package com.screenshotscout;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class ScreenshotScoutClientSupport {
  static final URI CAPTURE_ENDPOINT = URI.create("https://api.screenshotscout.com/v1/capture");

  private static final String SCREENSHOT_URL_HEADER = "Screenshot-Scout-Screenshot-URL";
  private static final String SCREENSHOT_URL_EXPIRES_AT_HEADER =
      "Screenshot-Scout-Screenshot-URL-Expires-At";
  private static final String CACHE_STATUS_HEADER = "Screenshot-Scout-Cache-Status";

  private final String accessKey;
  private final String secretKey;
  private final java.net.http.HttpClient httpClient;

  ScreenshotScoutClientSupport(ClientConfiguration configuration) {
    accessKey = configuration.accessKey();
    secretKey = configuration.secretKey();
    httpClient = configuration.httpClient();
  }

  java.net.http.HttpClient httpClient() {
    return httpClient;
  }

  PreparedCapture prepare(String targetUrl, CaptureOptions options, CaptureHttpMethod method) {
    if (method == null) {
      throw new ScreenshotScoutSerializationException(
          "The capture method must be CaptureHttpMethod.GET or CaptureHttpMethod.POST.", "method");
    }

    SerializedCaptureOptions serialized = CaptureSerializer.serialize(targetUrl, options);
    ExpectedResponseKind expectedKind = expectedResponseKind(options);
    String signature = createSignature(serialized.pairs());
    HttpRequest request = createRequest(serialized, method, signature);
    return new PreparedCapture(request, expectedKind);
  }

  String buildCaptureUrl(String targetUrl, CaptureOptions options) {
    SerializedCaptureOptions serialized = CaptureSerializer.serialize(targetUrl, options);
    String signature = createSignature(serialized.pairs());
    List<WirePair> pairs = new ArrayList<>(serialized.pairs().size() + 2);
    pairs.add(new WirePair("access_key", accessKey));
    pairs.addAll(serialized.pairs());
    if (signature != null) {
      pairs.add(new WirePair("signature", signature));
    }
    return CAPTURE_ENDPOINT + "?" + CaptureSerializer.encodeQuery(pairs);
  }

  CaptureResponse decode(HttpResponse<byte[]> response, ExpectedResponseKind expectedKind) {
    RawResponse rawResponse = createRawResponse(response);
    if (rawResponse.statusCode() < 200 || rawResponse.statusCode() > 299) {
      throw createApiException(rawResponse);
    }
    return decodeSuccessResponse(rawResponse, expectedKind);
  }

  private HttpRequest createRequest(
      SerializedCaptureOptions serialized, CaptureHttpMethod method, String signature) {
    try {
      URI requestUri;
      HttpRequest.BodyPublisher bodyPublisher;
      HttpRequest.Builder builder;
      if (method == CaptureHttpMethod.GET) {
        List<WirePair> pairs = new ArrayList<>(serialized.pairs());
        if (signature != null) {
          pairs.add(new WirePair("signature", signature));
        }
        requestUri = URI.create(CAPTURE_ENDPOINT + "?" + CaptureSerializer.encodeQuery(pairs));
        bodyPublisher = HttpRequest.BodyPublishers.noBody();
        builder = HttpRequest.newBuilder(requestUri).GET();
      } else {
        requestUri = CAPTURE_ENDPOINT;
        Map<String, Object> body = new LinkedHashMap<>(serialized.body());
        if (signature != null) {
          body.put("signature", signature);
        }
        bodyPublisher =
            HttpRequest.BodyPublishers.ofByteArray(CaptureSerializer.serializeJsonBody(body));
        builder =
            HttpRequest.newBuilder(requestUri)
                .header("Content-Type", "application/json")
                .POST(bodyPublisher);
      }
      return builder.header("Authorization", "Bearer " + accessKey).build();
    } catch (ScreenshotScoutException exception) {
      throw exception;
    } catch (RuntimeException cause) {
      throw new ScreenshotScoutSerializationException(
          "The Screenshot Scout HTTP request could not be constructed.", null, cause);
    }
  }

  private String createSignature(List<WirePair> pairs) {
    if (secretKey == null) {
      return null;
    }
    try {
      String canonicalQuery = CaptureSerializer.buildCanonicalQuery(pairs, accessKey);
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return java.util.HexFormat.of()
          .formatHex(mac.doFinal(canonicalQuery.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException | RuntimeException cause) {
      throw new ScreenshotScoutSerializationException(
          "The capture request could not be signed.", null, cause);
    }
  }

  private static RawResponse createRawResponse(HttpResponse<byte[]> response) {
    byte[] body = response.body();
    if (body == null) {
      throw new ScreenshotScoutResponseDecodingException(
          "Screenshot Scout returned an HTTP response without a buffered body.",
          new RawResponse(
              response.statusCode(),
              response.headers(),
              response.headers().firstValue("content-type").orElse(null),
              new byte[0]),
          new IOException("Expected a buffered response body."));
    }
    return new RawResponse(
        response.statusCode(),
        response.headers(),
        response.headers().firstValue("content-type").orElse(null),
        body);
  }

  private static ScreenshotScoutApiException createApiException(RawResponse rawResponse) {
    boolean hasResponseBody = false;
    Object responseBody = null;
    try {
      responseBody = JsonCodec.parse(rawResponse.bodyUnsafe());
      hasResponseBody = true;
    } catch (IOException ignored) {
      // Raw response access never depends on successful JSON parsing.
    }

    String errorCode = null;
    String errorMessage = null;
    List<Object> errors = null;
    if (responseBody instanceof Map<?, ?> objectBody) {
      Object code = objectBody.get("error_code");
      if (code instanceof String stringCode) {
        errorCode = stringCode;
      }
      Object message = objectBody.get("error_message");
      if (message instanceof String stringMessage) {
        errorMessage = stringMessage;
      }
      Object errorValues = objectBody.get("errors");
      if (errorValues instanceof List<?> list) {
        errors = new ArrayList<>(list);
      }
    }
    return new ScreenshotScoutApiException(
        rawResponse, hasResponseBody, responseBody, errorCode, errorMessage, errors);
  }

  private static CaptureResponse decodeSuccessResponse(
      RawResponse rawResponse, ExpectedResponseKind expectedKind) {
    ExpectedResponseKind actualKind =
        isJsonMediaType(rawResponse.contentType().orElse(null))
            ? ExpectedResponseKind.JSON
            : ExpectedResponseKind.BINARY;
    if (expectedKind != null && actualKind != expectedKind) {
      String actual = actualKind == ExpectedResponseKind.JSON ? "json" : "binary";
      String expected = expectedKind == ExpectedResponseKind.JSON ? "json" : "binary";
      throw new ScreenshotScoutResponseDecodingException(
          "Screenshot Scout returned a successful "
              + actual
              + " response when "
              + expected
              + " was requested.",
          rawResponse,
          new IOException("Expected a " + expected + " response but received " + actual + "."));
    }
    return actualKind == ExpectedResponseKind.JSON
        ? decodeJsonResponse(rawResponse)
        : decodeBinaryResponse(rawResponse);
  }

  private static BinaryCaptureResponse decodeBinaryResponse(RawResponse rawResponse) {
    return new DefaultBinaryCaptureResponse(
        rawResponse,
        rawResponse.headers().firstValue(SCREENSHOT_URL_HEADER).orElse(null),
        rawResponse.headers().firstValue(SCREENSHOT_URL_EXPIRES_AT_HEADER).orElse(null),
        rawResponse.headers().firstValue(CACHE_STATUS_HEADER).orElse(null));
  }

  private static JsonCaptureResponse decodeJsonResponse(RawResponse rawResponse) {
    Object value;
    try {
      value = JsonCodec.parse(rawResponse.bodyUnsafe());
    } catch (IOException cause) {
      throw new ScreenshotScoutResponseDecodingException(
          "Screenshot Scout returned a successful JSON response that could not be decoded.",
          rawResponse,
          cause);
    }
    if (!(value instanceof Map<?, ?> objectBody)) {
      throw new ScreenshotScoutResponseDecodingException(
          "Screenshot Scout returned a successful JSON response that was not an object.",
          rawResponse,
          new IOException("Expected a JSON object."));
    }

    String screenshotUrl = readOptionalString(objectBody, "screenshot_url", rawResponse);
    String screenshotUrlExpiresAt =
        readOptionalString(objectBody, "screenshot_url_expires_at", rawResponse);
    String cacheStatus = readOptionalString(objectBody, "cache_status", rawResponse);
    Map<String, Object> additionalFields = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : objectBody.entrySet()) {
      String key = (String) entry.getKey();
      if (!key.equals("screenshot_url")
          && !key.equals("screenshot_url_expires_at")
          && !key.equals("cache_status")) {
        additionalFields.put(key, entry.getValue());
      }
    }
    return new DefaultJsonCaptureResponse(
        new CaptureResult(screenshotUrl, screenshotUrlExpiresAt, cacheStatus, additionalFields),
        rawResponse);
  }

  private static String readOptionalString(Map<?, ?> object, String key, RawResponse rawResponse) {
    if (!object.containsKey(key)) {
      return null;
    }
    Object value = object.get(key);
    if (!(value instanceof String stringValue)) {
      throw new ScreenshotScoutResponseDecodingException(
          "Screenshot Scout returned a non-string \"" + key + "\" JSON field.",
          rawResponse,
          new IOException("Expected \"" + key + "\" to be a string."));
    }
    return stringValue;
  }

  private static boolean isJsonMediaType(String contentType) {
    if (contentType == null) {
      return false;
    }
    int separator = contentType.indexOf(';');
    String mediaType =
        (separator < 0 ? contentType : contentType.substring(0, separator))
            .trim()
            .toLowerCase(Locale.ROOT);
    return mediaType.equals("application/json") || mediaType.endsWith("+json");
  }

  private static ExpectedResponseKind expectedResponseKind(CaptureOptions options) {
    CaptureResponseType responseType = options == null ? null : options.responseType();
    if (responseType == null || responseType.equals(CaptureResponseType.BINARY)) {
      return ExpectedResponseKind.BINARY;
    }
    return responseType.equals(CaptureResponseType.JSON) ? ExpectedResponseKind.JSON : null;
  }
}
