package com.screenshotscout;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CaptureSerializationTest {
  @Test
  void postIsDefaultAndSerializesTheCompleteTypedOptionSurface() throws Exception {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClient client = client(transport, "access-key", null);
    CaptureOptions options =
        CaptureOptions.builder()
            .format(CaptureFormat.fromString("future-format"))
            .responseType(CaptureResponseType.fromString("future-response"))
            .country("")
            .proxy("")
            .geolocationLatitude(0.0)
            .geolocationLongitude(0.0)
            .geolocationAccuracy(0.0)
            .cookies(List.of("session=a", "session=a", ""))
            .headers(List.of("X-Test:one", "X-Test:one", ""))
            .timeout(0)
            .waitUntil(CaptureWaitUntil.fromString("future-wait-state"))
            .navigationTimeout(0)
            .delay(0)
            .device("")
            .deviceViewportWidth(0)
            .deviceViewportHeight(0)
            .deviceScaleFactor(0.0)
            .deviceIsMobile(false)
            .deviceHasTouch(false)
            .deviceUserAgent("")
            .timezone("")
            .mediaType(CaptureMediaType.fromString("future-media"))
            .colorScheme(CaptureColorScheme.fromString("future-scheme"))
            .reducedMotion(false)
            .fullPage(false)
            .fullPagePreScroll(false)
            .fullPagePreScrollStep(0)
            .fullPagePreScrollStepDelay(0)
            .fullPageMaxHeight(0)
            .blockCookieBanners(false)
            .blockAds(false)
            .blockChatWidgets(false)
            .hideSelectors(List.of(".same", ".same", ""))
            .clickSelectors(List.of("#first", "#first", ""))
            .clickAllSelectors(List.of(".all", ".all", ""))
            .injectCss(List.of("", "body { color: red; }"))
            .injectJs(List.of("", "document.title = 'x';"))
            .bypassCsp(false)
            .selector("")
            .clipX(0)
            .clipY(0)
            .clipWidth(0)
            .clipHeight(0)
            .imageWidth(0)
            .imageHeight(0)
            .imageMode(CaptureImageMode.fromString("future-image-mode"))
            .imageAnchor(CaptureImageAnchor.fromString("future-anchor"))
            .imageAllowUpscale(false)
            .imageBackground("")
            .imageQuality(0)
            .pdfPaperFormat(CapturePdfPaperFormat.fromString("future-paper"))
            .pdfLandscape(false)
            .pdfPrintBackground(false)
            .pdfMargin("")
            .pdfMarginTop("")
            .pdfMarginRight("")
            .pdfMarginBottom("")
            .pdfMarginLeft("")
            .pdfScale(0.0)
            .pdfPageRanges("")
            .cache(false)
            .cacheTtl(0)
            .cacheKey("")
            .storageMode(CaptureStorageMode.fromString("future-storage"))
            .storageEndpoint("")
            .storageBucket("")
            .storageRegion("")
            .storageObjectKey("")
            .build();

    assertInstanceOf(BinaryCaptureResponse.class, client.capture("", options));

    assertEquals(1, transport.requests().size());
    HttpRequest request = transport.requests().get(0);
    assertEquals("https://api.screenshotscout.com/v1/capture", request.uri().toString());
    assertEquals("POST", request.method());
    assertEquals("Bearer access-key", request.headers().firstValue("authorization").orElseThrow());
    assertEquals("application/json", request.headers().firstValue("content-type").orElseThrow());
    assertTrue(request.timeout().isEmpty());

    @SuppressWarnings("unchecked")
    Map<String, Object> body =
        (Map<String, Object>) JsonCodec.parse(TestHttpClient.requestBody(request));
    Map<String, Object> expected = new LinkedHashMap<>();
    expected.put("url", "");
    expected.put("format", "future-format");
    expected.put("response_type", "future-response");
    expected.put("country", "");
    expected.put("proxy", "");
    expected.put("geolocation_latitude", 0);
    expected.put("geolocation_longitude", 0);
    expected.put("geolocation_accuracy", 0);
    expected.put("cookies", List.of("session=a", "session=a", ""));
    expected.put("headers", List.of("X-Test:one", "X-Test:one", ""));
    expected.put("timeout", 0);
    expected.put("wait_until", "future-wait-state");
    expected.put("navigation_timeout", 0);
    expected.put("delay", 0);
    expected.put("device", "");
    expected.put("device_viewport_width", 0);
    expected.put("device_viewport_height", 0);
    expected.put("device_scale_factor", 0);
    expected.put("device_is_mobile", false);
    expected.put("device_has_touch", false);
    expected.put("device_user_agent", "");
    expected.put("timezone", "");
    expected.put("media_type", "future-media");
    expected.put("color_scheme", "future-scheme");
    expected.put("reduced_motion", false);
    expected.put("full_page", false);
    expected.put("full_page_pre_scroll", false);
    expected.put("full_page_pre_scroll_step", 0);
    expected.put("full_page_pre_scroll_step_delay", 0);
    expected.put("full_page_max_height", 0);
    expected.put("block_cookie_banners", false);
    expected.put("block_ads", false);
    expected.put("block_chat_widgets", false);
    expected.put("hide_selectors", List.of(".same", ".same", ""));
    expected.put("click_selectors", List.of("#first", "#first", ""));
    expected.put("click_all_selectors", List.of(".all", ".all", ""));
    expected.put("inject_css", List.of("", "body { color: red; }"));
    expected.put("inject_js", List.of("", "document.title = 'x';"));
    expected.put("bypass_csp", false);
    expected.put("selector", "");
    expected.put("clip_x", 0);
    expected.put("clip_y", 0);
    expected.put("clip_width", 0);
    expected.put("clip_height", 0);
    expected.put("image_width", 0);
    expected.put("image_height", 0);
    expected.put("image_mode", "future-image-mode");
    expected.put("image_anchor", "future-anchor");
    expected.put("image_allow_upscale", false);
    expected.put("image_background", "");
    expected.put("image_quality", 0);
    expected.put("pdf_paper_format", "future-paper");
    expected.put("pdf_landscape", false);
    expected.put("pdf_print_background", false);
    expected.put("pdf_margin", "");
    expected.put("pdf_margin_top", "");
    expected.put("pdf_margin_right", "");
    expected.put("pdf_margin_bottom", "");
    expected.put("pdf_margin_left", "");
    expected.put("pdf_scale", 0);
    expected.put("pdf_page_ranges", "");
    expected.put("cache", false);
    expected.put("cache_ttl", 0);
    expected.put("cache_key", "");
    expected.put("storage_mode", "future-storage");
    expected.put("storage_endpoint", "");
    expected.put("storage_bucket", "");
    expected.put("storage_region", "");
    expected.put("storage_object_key", "");
    assertEquals(expected, body);
  }

  @ParameterizedTest
  @MethodSource("ecmaScriptNumberCases")
  void doubleOptionsUseEcmaScriptFormattingInQueriesAndJson(double value, String expected)
      throws Exception {
    SerializedCaptureOptions serialized =
        CaptureSerializer.serialize(
            "https://example.com", CaptureOptions.builder().pdfScale(value).build());

    assertEquals(new WirePair("pdf_scale", expected), serialized.pairs().get(1));
    assertEquals(
        "{\"url\":\"https://example.com\",\"pdf_scale\":" + expected + "}",
        new String(CaptureSerializer.serializeJsonBody(serialized.body()), StandardCharsets.UTF_8));
  }

  @Test
  void signedPostWithAWholeDoubleMatchesApiCanonicalization() throws Exception {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClient client = client(transport, "ak_test", "sk_test");
    String expectedSignature = "9455e37a8893f7a2e43d5bdf8203986139f66f768bd7ccebb8c8df80604a9683";

    client.capture("https://example.com", CaptureOptions.builder().pdfScale(2.0).build());

    assertEquals(
        "{\"url\":\"https://example.com\",\"pdf_scale\":2,\"signature\":\""
            + expectedSignature
            + "\"}",
        new String(
            TestHttpClient.requestBody(transport.requests().get(0)), StandardCharsets.UTF_8));
  }

  @Test
  void nullValuesAndEmptyRepeatedCollectionsAreOmittedWithoutDefaults() throws Exception {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClient client = client(transport, "key", null);
    CaptureOptions options =
        CaptureOptions.builder()
            .format(null)
            .cookies(List.of())
            .headers(List.of())
            .hideSelectors(List.of())
            .clickSelectors(List.of())
            .clickAllSelectors(List.of())
            .injectCss(List.of())
            .injectJs(List.of())
            .build();

    client.capture("https://example.com", options);

    @SuppressWarnings("unchecked")
    Map<String, Object> body =
        (Map<String, Object>)
            JsonCodec.parse(TestHttpClient.requestBody(transport.requests().get(0)));
    assertEquals(Map.of("url", "https://example.com"), body);
  }

  @Test
  void getUsesRepeatedParametersInCallerOrderAndBearerOnly() throws Exception {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClient client = client(transport, "query-secret", null);
    CaptureOptions options =
        CaptureOptions.builder()
            .cookies(List.of("a=1", "a=1"))
            .headers(List.of("X-Test:one", "X-Test:one"))
            .delay(0)
            .fullPage(false)
            .cacheKey("")
            .build();

    client.capture("https://example.com/a path", options, CaptureHttpMethod.GET);

    HttpRequest request = transport.requests().get(0);
    assertEquals("GET", request.method());
    assertTrue(request.bodyPublisher().isEmpty());
    assertTrue(request.headers().firstValue("content-type").isEmpty());
    assertEquals(
        "Bearer query-secret", request.headers().firstValue("authorization").orElseThrow());
    assertEquals(
        "url=https%3A%2F%2Fexample.com%2Fa+path&cookies=a%3D1&cookies=a%3D1&headers=X-Test%3Aone&headers=X-Test%3Aone&delay=0&full_page=false&cache_key=",
        request.uri().getRawQuery());
    Map<String, List<String>> query = TestHttpClient.queryParameters(request.uri());
    assertFalse(query.containsKey("access_key"));
    assertFalse(query.containsKey("signature"));
    assertEquals(List.of("a=1", "a=1"), query.get("cookies"));
  }

  @Test
  void buildCaptureUrlIsPureAndIncludesOnlyTheAccessCredential() {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClient client = client(transport, "query-key", null);
    CaptureOptions options =
        CaptureOptions.builder()
            .cookies(List.of("a=1", "a=1"))
            .headers(List.of("X-Test:one", "X-Test:one"))
            .delay(0)
            .fullPage(false)
            .cacheKey("")
            .build();

    String url = client.buildCaptureUrl("https://example.com/a path", options);

    assertTrue(transport.requests().isEmpty());
    assertEquals(
        "https://api.screenshotscout.com/v1/capture?access_key=query-key&url=https%3A%2F%2Fexample.com%2Fa+path&cookies=a%3D1&cookies=a%3D1&headers=X-Test%3Aone&headers=X-Test%3Aone&delay=0&full_page=false&cache_key=",
        url);
  }

  @Test
  void getPostAndGeneratedUrlSignaturesMatchTheApiAlgorithm() throws Exception {
    TestHttpClient transport = new TestHttpClient();
    transport.respond(
        200,
        Map.of("content-type", List.of("application/json")),
        "{}".getBytes(StandardCharsets.UTF_8));
    ScreenshotScoutClient client = client(transport, "ak_test", "sk_test");
    CaptureOptions options =
        CaptureOptions.builder()
            .responseType(CaptureResponseType.JSON)
            .fullPage(false)
            .delay(0)
            .headers(List.of("X-Test:one", "X-Test:two"))
            .build();
    String target = "https://example.com/a b?x=1&y=2";
    String expected = "0c4928ba691575903f27b911b8ea1a536604ca070d60d886e10c127c05e236fc";

    client.capture(target, options, CaptureHttpMethod.GET);
    client.capture(target, options, CaptureHttpMethod.POST);
    String built = client.buildCaptureUrl(target, options);

    Map<String, List<String>> getQuery =
        TestHttpClient.queryParameters(transport.requests().get(0).uri());
    @SuppressWarnings("unchecked")
    Map<String, Object> postBody =
        (Map<String, Object>)
            JsonCodec.parse(TestHttpClient.requestBody(transport.requests().get(1)));
    Map<String, List<String>> builtQuery =
        TestHttpClient.queryParameters(java.net.URI.create(built));
    assertEquals(List.of(expected), getQuery.get("signature"));
    assertEquals(expected, postBody.get("signature"));
    assertEquals(List.of(expected), builtQuery.get("signature"));
    assertEquals(List.of("ak_test"), builtQuery.get("access_key"));
    assertFalse(getQuery.containsKey("access_key"));
    assertFalse(postBody.containsKey("access_key"));
    assertFalse(built.contains("sk_test"));
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void unsafeValuesFailLocallyWithoutCallingTheTransport() {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClient client = client(transport, "key", null);

    ScreenshotScoutSerializationException numberError =
        assertThrows(
            ScreenshotScoutSerializationException.class,
            () ->
                client.capture(
                    "https://example.com",
                    CaptureOptions.builder().imageWidth(1).pdfScale(Double.NaN).build()));
    assertEquals("pdfScale", numberError.option().orElseThrow());

    List invalidHeaders = List.of("X-Test:one", 1);
    ScreenshotScoutSerializationException repeatedError =
        assertThrows(
            ScreenshotScoutSerializationException.class,
            () ->
                client.capture(
                    "https://example.com",
                    CaptureOptions.builder().headers((List<String>) invalidHeaders).build()));
    assertEquals("headers", repeatedError.option().orElseThrow());

    assertThrows(
        ScreenshotScoutSerializationException.class,
        () -> client.capture(null, CaptureOptions.builder().build()));
    assertThrows(
        ScreenshotScoutSerializationException.class,
        () -> client.capture("https://example.com", null, null));
    assertTrue(transport.requests().isEmpty());
  }

  @Test
  void builtOptionsCopyAndProtectRepeatedCollections() {
    List<String> mutable = new ArrayList<>(List.of("a=1", "a=1"));
    CaptureOptions options = CaptureOptions.builder().cookies(mutable).build();
    mutable.add("b=2");

    assertEquals(List.of("a=1", "a=1"), options.cookies());
    assertThrows(UnsupportedOperationException.class, () -> options.cookies().add("c=3"));
    assertArrayEquals(
        "future".getBytes(StandardCharsets.UTF_8),
        CaptureFormat.fromString("future").value().getBytes(StandardCharsets.UTF_8));
  }

  private static Stream<Arguments> ecmaScriptNumberCases() {
    return Stream.of(
        Arguments.of(0.0, "0"),
        Arguments.of(-0.0, "0"),
        Arguments.of(2.0, "2"),
        Arguments.of(1.25, "1.25"),
        Arguments.of(0.000001, "0.000001"),
        Arguments.of(0.0000001, "1e-7"),
        Arguments.of(10_000_000.0, "10000000"),
        Arguments.of(100_000_000_000_000_000_000.0, "100000000000000000000"),
        Arguments.of(1e21, "1e+21"),
        Arguments.of(-1.2e-7, "-1.2e-7"),
        Arguments.of(1_000_000_000_000_000_128.0, "1000000000000000100"),
        Arguments.of(Double.MIN_VALUE, "5e-324"),
        Arguments.of(Double.MAX_VALUE, "1.7976931348623157e+308"));
  }

  private static ScreenshotScoutClient client(
      TestHttpClient transport, String accessKey, String secretKey) {
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder().accessKey(accessKey).httpClient(transport);
    if (secretKey != null) {
      builder.secretKey(secretKey);
    }
    return builder.build();
  }
}
