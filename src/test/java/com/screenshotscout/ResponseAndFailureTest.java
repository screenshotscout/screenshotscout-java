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
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ResponseAndFailureTest {
  private static final String HTML = "<!doctype html><title>Invoice #1042</title>";

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
    assertTrue(response.markdown().isEmpty());
    assertTrue(response.html().isEmpty());
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

  @ParameterizedTest
  @ValueSource(strings = {"text/markdown; charset=utf-8", "TEXT/MARKDOWN"})
  void markdownResponseRetainsBytesAndDecodesTheText(String contentType) throws Exception {
    String markdown = "# Привет\n\nHello, world.\n";
    byte[] body = markdown.getBytes(StandardCharsets.UTF_8);
    TestHttpClient transport = new TestHttpClient();
    transport.respond(200, Map.of("content-type", List.of(contentType)), body);

    BinaryCaptureResponse response =
        assertInstanceOf(
            BinaryCaptureResponse.class,
            client(transport)
                .capture(
                    "https://example.com",
                    CaptureOptions.builder().format(CaptureFormat.MD).build()));

    assertEquals(markdown, response.markdown().orElseThrow());
    assertTrue(response.html().isEmpty());
    assertArrayEquals(body, response.bytes());
    assertEquals(contentType, response.rawResponse().contentType().orElseThrow());
  }

  @ParameterizedTest
  @ValueSource(strings = {"text/html; charset=utf-8", "TEXT/HTML; charset=UTF-8"})
  void htmlResponseRetainsBytesAndDecodesTheText(String contentType) throws Exception {
    String html = "<!DOCTYPE html><html><body><h1>Привет</h1><p>Hello, world.</p></body></html>";
    byte[] body = html.getBytes(StandardCharsets.UTF_8);
    TestHttpClient transport = new TestHttpClient();
    transport.respond(200, Map.of("content-type", List.of(contentType)), body);

    BinaryCaptureResponse response =
        assertInstanceOf(
            BinaryCaptureResponse.class,
            client(transport)
                .capture(
                    "https://example.com",
                    CaptureOptions.builder().format(CaptureFormat.HTML).build()));

    assertEquals(html, response.html().orElseThrow());
    assertTrue(response.markdown().isEmpty());
    assertArrayEquals(body, response.bytes());
    assertEquals(contentType, response.rawResponse().contentType().orElseThrow());
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

  @Test
  void htmlCapturesDecodeResponsesTheSameWayAsUrlCaptures() throws Exception {
    byte[] pdf = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);
    TestHttpClient binaryTransport = new TestHttpClient();
    binaryTransport.respond(
        200,
        Map.of(
            "content-type",
            List.of("application/pdf"),
            "screenshot-scout-screenshot-url",
            List.of("https://cdn.example/invoice.pdf")),
        pdf);
    CaptureOptions pdfOptions = CaptureOptions.builder().format(CaptureFormat.PDF).build();

    for (CaptureResponse response :
        List.of(
            client(binaryTransport).captureFromHtml(HTML, pdfOptions),
            asyncClient(binaryTransport).captureFromHtml(HTML, pdfOptions).join())) {
      BinaryCaptureResponse binary = assertInstanceOf(BinaryCaptureResponse.class, response);
      assertArrayEquals(pdf, binary.bytes());
      assertEquals("https://cdn.example/invoice.pdf", binary.screenshotUrl().orElseThrow());
      assertEquals("application/pdf", binary.rawResponse().contentType().orElseThrow());
    }

    TestHttpClient jsonTransport = new TestHttpClient();
    jsonTransport.respond(
        200,
        Map.of("content-type", List.of("application/json")),
        "{\"screenshot_url\":\"https://cdn.example/invoice.pdf\"}"
            .getBytes(StandardCharsets.UTF_8));
    CaptureOptions jsonOptions =
        CaptureOptions.builder()
            .format(CaptureFormat.PDF)
            .responseType(CaptureResponseType.JSON)
            .build();

    for (CaptureResponse response :
        List.of(
            client(jsonTransport).captureFromHtml(HTML, jsonOptions),
            asyncClient(jsonTransport).captureFromHtml(HTML, jsonOptions).join())) {
      JsonCaptureResponse json = assertInstanceOf(JsonCaptureResponse.class, response);
      assertEquals("https://cdn.example/invoice.pdf", json.result().screenshotUrl().orElseThrow());
    }
  }

  @Test
  void aBodyOverTheSizeLimitArrivesAsAnApiException() {
    String encoded =
        "{\"error_code\":\"request_too_large\","
            + "\"error_message\":\"The request body must be at most 5 MB.\"}";
    TestHttpClient transport = new TestHttpClient();
    transport.respond(
        413,
        Map.of("content-type", List.of("application/json")),
        encoded.getBytes(StandardCharsets.UTF_8));

    ScreenshotScoutApiException blockingError =
        assertThrows(
            ScreenshotScoutApiException.class, () -> client(transport).captureFromHtml(HTML));
    CompletionException completion =
        assertThrows(
            CompletionException.class, () -> asyncClient(transport).captureFromHtml(HTML).join());
    ScreenshotScoutApiException asyncError =
        assertInstanceOf(ScreenshotScoutApiException.class, completion.getCause());

    for (ScreenshotScoutApiException error : List.of(blockingError, asyncError)) {
      assertEquals(413, error.statusCode());
      assertEquals("request_too_large", error.errorCode().orElseThrow());
      assertEquals("The request body must be at most 5 MB.", error.errorMessage().orElseThrow());
      assertEquals("The request body must be at most 5 MB.", error.getMessage());
      assertTrue(error.hasResponseBody());
      assertArrayEquals(encoded.getBytes(StandardCharsets.UTF_8), error.rawResponse().body());
    }
    assertEquals(1, transport.blockingCalls());
    assertEquals(1, transport.asyncCalls());
  }

  private static ScreenshotScoutClient client(TestHttpClient transport) {
    return ScreenshotScoutClient.builder().accessKey("key").httpClient(transport).build();
  }

  private static ScreenshotScoutAsyncClient asyncClient(TestHttpClient transport) {
    return ScreenshotScoutAsyncClient.builder().accessKey("key").httpClient(transport).buildAsync();
  }
}
