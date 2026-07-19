package com.screenshotscout;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ResponseAndFailureTest {
  @Test
  void binaryResponseRetainsBytesMetadataAndRawResponse() throws Exception {
    byte[] body = new byte[] {0, 1, 2, (byte) 255};
    TestHttpClient transport = new TestHttpClient();
    transport.respond(
        201,
        Map.of(
            "content-type",
            List.of("image/png"),
            "screenshot-scout-cache-status",
            List.of("HIT"),
            "screenshot-scout-screenshot-url",
            List.of("https://cdn.example/screenshot.png"),
            "screenshot-scout-screenshot-url-expires-at",
            List.of("2026-07-14T00:00:00Z"),
            "set-cookie",
            List.of("a=1; Path=/", "b=2; Path=/")),
        body);
    ScreenshotScoutClient client = client(transport);

    BinaryCaptureResponse response =
        assertInstanceOf(BinaryCaptureResponse.class, client.capture("https://example.com"));

    assertArrayEquals(body, response.bytes());
    assertEquals("https://cdn.example/screenshot.png", response.screenshotUrl().orElseThrow());
    assertEquals("2026-07-14T00:00:00Z", response.screenshotUrlExpiresAt().orElseThrow());
    assertEquals("HIT", response.cacheStatus().orElseThrow());
    assertEquals(201, response.rawResponse().statusCode());
    assertEquals("image/png", response.rawResponse().contentType().orElseThrow());
    assertEquals(
        List.of("a=1; Path=/", "b=2; Path=/"),
        response.rawResponse().headers().allValues("set-cookie"));
    assertArrayEquals(body, response.rawResponse().body());

    byte[] returned = response.bytes();
    returned[0] = 99;
    assertEquals(0, response.bytes()[0]);
  }

  @Test
  void jsonResponseRetainsDocumentedAndAdditionalFields() throws Exception {
    String encoded =
        "{\"screenshot_url\":\"https://cdn.example/screenshot.png\","
            + "\"cache_status\":\"miss\",\"request_id\":\"request-123\","
            + "\"future\":{\"nested\":true},\"nullable\":null}";
    TestHttpClient transport = new TestHttpClient();
    transport.respond(
        200,
        Map.of("content-type", List.of("application/vnd.screenshotscout+json; charset=utf-8")),
        encoded.getBytes(StandardCharsets.UTF_8));
    ScreenshotScoutClient client = client(transport);

    JsonCaptureResponse response =
        assertInstanceOf(
            JsonCaptureResponse.class,
            client.capture(
                "https://example.com",
                CaptureOptions.builder().responseType(CaptureResponseType.JSON).build()));

    assertEquals(
        "https://cdn.example/screenshot.png", response.result().screenshotUrl().orElseThrow());
    assertEquals("miss", response.result().cacheStatus().orElseThrow());
    assertTrue(response.result().screenshotUrlExpiresAt().isEmpty());
    assertEquals("request-123", response.result().additionalFields().get("request_id"));
    assertEquals(Map.of("nested", true), response.result().additionalFields().get("future"));
    assertTrue(response.result().additionalFields().containsKey("nullable"));
    assertEquals(null, response.result().additionalFields().get("nullable"));
    assertArrayEquals(encoded.getBytes(StandardCharsets.UTF_8), response.rawResponse().body());
    assertThrows(
        UnsupportedOperationException.class,
        () -> response.result().additionalFields().put("x", true));
  }

  @Test
  void knownResponseTypesRejectSuccessfulWrongMediaTypes() {
    TestHttpClient jsonTransport = new TestHttpClient();
    jsonTransport.respond(
        200,
        Map.of("content-type", List.of("application/json")),
        "{}".getBytes(StandardCharsets.UTF_8));
    ScreenshotScoutResponseDecodingException jsonError =
        assertThrows(
            ScreenshotScoutResponseDecodingException.class,
            () -> client(jsonTransport).capture("https://example.com"));
    assertTrue(jsonError.getMessage().contains("json response when binary was requested"));
    assertEquals("application/json", jsonError.rawResponse().contentType().orElseThrow());

    TestHttpClient binaryTransport = new TestHttpClient();
    ScreenshotScoutResponseDecodingException binaryError =
        assertThrows(
            ScreenshotScoutResponseDecodingException.class,
            () ->
                client(binaryTransport)
                    .capture(
                        "https://example.com",
                        CaptureOptions.builder().responseType(CaptureResponseType.JSON).build()));
    assertTrue(binaryError.getMessage().contains("binary response when json was requested"));
    assertEquals("image/png", binaryError.rawResponse().contentType().orElseThrow());
  }

  @Test
  void openDynamicResponseTypeUsesActualMediaType() throws Exception {
    TestHttpClient transport = new TestHttpClient();
    transport.respond(
        200,
        Map.of("content-type", List.of("application/json")),
        "{\"screenshot_url\":\"https://cdn.example/a.png\"}".getBytes(StandardCharsets.UTF_8));

    CaptureResponse response =
        client(transport)
            .capture(
                "https://example.com",
                CaptureOptions.builder()
                    .responseType(CaptureResponseType.fromString("future-response"))
                    .build());

    assertInstanceOf(JsonCaptureResponse.class, response);
  }

  @Test
  void malformedSuccessfulJsonIsADecodingFailureWithRawAccess() {
    for (String body : List.of("{", "[]", "{\"screenshot_url\":123}")) {
      TestHttpClient transport = new TestHttpClient();
      transport.respond(
          200,
          Map.of("content-type", List.of("application/json")),
          body.getBytes(StandardCharsets.UTF_8));

      ScreenshotScoutResponseDecodingException error =
          assertThrows(
              ScreenshotScoutResponseDecodingException.class,
              () ->
                  client(transport)
                      .capture(
                          "https://example.com",
                          CaptureOptions.builder().responseType(CaptureResponseType.JSON).build()));

      assertEquals(200, error.rawResponse().statusCode());
      assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), error.rawResponse().body());
    }
  }

  @Test
  void apiFailuresRetainParsedFieldsAdditionalDataAndRawResponse() {
    String encoded =
        "{\"error_code\":\"invalid_options\","
            + "\"error_message\":\"One or more options are invalid.\","
            + "\"errors\":[{\"option\":\"format\",\"message\":\"Unsupported.\"}],"
            + "\"request_id\":\"request-456\"}";
    TestHttpClient transport = new TestHttpClient();
    transport.respond(
        400,
        Map.of(
            "content-type",
            List.of("application/json; charset=utf-8"),
            "x-request-id",
            List.of("request-456")),
        encoded.getBytes(StandardCharsets.UTF_8));

    ScreenshotScoutApiException error =
        assertThrows(
            ScreenshotScoutApiException.class,
            () -> client(transport).capture("not-semantically-validated"));

    assertEquals(400, error.statusCode());
    assertEquals("invalid_options", error.errorCode().orElseThrow());
    assertEquals("One or more options are invalid.", error.errorMessage().orElseThrow());
    assertTrue(error.hasResponseBody());
    assertInstanceOf(Map.class, error.responseBody());
    assertEquals(1, error.errors().orElseThrow().size());
    assertEquals(
        "request-456", error.rawResponse().headers().firstValue("x-request-id").orElseThrow());
    assertArrayEquals(encoded.getBytes(StandardCharsets.UTF_8), error.rawResponse().body());
  }

  @Test
  void nonJsonRedirectFailureExposesRawResponseWithoutRetrying() {
    TestHttpClient transport = new TestHttpClient();
    transport.respond(
        302,
        Map.of("location", List.of("https://other.example/v1/capture")),
        "redirect".getBytes(StandardCharsets.UTF_8));

    ScreenshotScoutApiException error =
        assertThrows(
            ScreenshotScoutApiException.class,
            () -> client(transport).capture("https://example.com"));

    assertEquals(302, error.statusCode());
    assertTrue(error.errorCode().isEmpty());
    assertFalse(error.hasResponseBody());
    assertArrayEquals("redirect".getBytes(StandardCharsets.UTF_8), error.rawResponse().body());
    assertEquals(1, transport.blockingCalls());
  }

  private static ScreenshotScoutClient client(TestHttpClient transport) {
    return ScreenshotScoutClient.builder().accessKey("key").httpClient(transport).build();
  }
}
