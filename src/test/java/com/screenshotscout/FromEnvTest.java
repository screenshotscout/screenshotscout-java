package com.screenshotscout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// Every test injects its own environment lookup, so the real environment never matters here.
class FromEnvTest {
  private static final String ACCESS = "SCREENSHOTSCOUT_ACCESS_KEY";
  private static final String SECRET = "SCREENSHOTSCOUT_SECRET_KEY";
  private static final Map<String, String> BOTH_KEYS = Map.of(ACCESS, "ak_test", SECRET, "sk_test");
  private static final String MISSING_ACCESS_KEY =
      "A non-blank Screenshot Scout access key is required. Pass one, or set"
          + " SCREENSHOTSCOUT_ACCESS_KEY.";

  // The "SDK interoperability vector" from signing-vectors.json, signed with ak_test and sk_test.
  private static final String TARGET = "https://example.com/a b?x=1&y=2";
  private static final CaptureOptions OPTIONS =
      CaptureOptions.builder()
          .responseType(CaptureResponseType.JSON)
          .fullPage(false)
          .delay(0)
          .headers(List.of("X-Test:one", "X-Test:two"))
          .build();
  private static final String SIGNATURE =
      "6419f2ac8c0440f1e341b547630606c9dc74dacfbb2df8e122fc9ae26831d893";

  /** Creates a client from an environment lookup and returns the capture URL it builds. */
  @FunctionalInterface
  private interface ClientFactory {
    String buildCaptureUrl(Function<String, String> environment);
  }

  private static Stream<Arguments> factories() {
    ClientFactory client =
        environment -> ScreenshotScoutClient.fromEnv(environment).buildCaptureUrl(TARGET, OPTIONS);
    ClientFactory asyncClient =
        environment ->
            ScreenshotScoutAsyncClient.fromEnv(environment).buildCaptureUrl(TARGET, OPTIONS);
    ClientFactory builder =
        environment ->
            ScreenshotScoutClient.builder()
                .environment(environment)
                .fromEnv()
                .httpClient(new TestHttpClient())
                .build()
                .buildCaptureUrl(TARGET, OPTIONS);
    ClientFactory asyncBuilder =
        environment ->
            ScreenshotScoutAsyncClient.builder()
                .environment(environment)
                .fromEnv()
                .httpClient(new TestHttpClient())
                .buildAsync()
                .buildCaptureUrl(TARGET, OPTIONS);
    return Stream.of(
        Arguments.of("ScreenshotScoutClient.fromEnv()", client),
        Arguments.of("ScreenshotScoutAsyncClient.fromEnv()", asyncClient),
        Arguments.of("builder().fromEnv().build()", builder),
        Arguments.of("builder().fromEnv().buildAsync()", asyncBuilder));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("factories")
  void bothVariablesGiveAClientThatSigns(String name, ClientFactory factory) {
    Map<String, List<String>> query = query(factory.buildCaptureUrl(BOTH_KEYS::get));

    assertEquals(List.of("ak_test"), query.get("access_key"));
    assertEquals(List.of(SIGNATURE), query.get("signature"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("factories")
  void anUnsetOrEmptySecretVariableMeansNoSecretKey(String name, ClientFactory factory) {
    List<Map<String, String>> environments =
        List.of(Map.of(ACCESS, "ak_test"), Map.of(ACCESS, "ak_test", SECRET, ""));

    for (Map<String, String> environment : environments) {
      Map<String, List<String>> query = query(factory.buildCaptureUrl(environment::get));
      assertEquals(List.of("ak_test"), query.get("access_key"));
      assertFalse(query.containsKey("signature"));
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("factories")
  void aMissingOrEmptyAccessVariableIsAnErrorThatNamesIt(String name, ClientFactory factory) {
    List<Map<String, String>> environments =
        List.of(Map.of(), Map.of(ACCESS, ""), Map.of(SECRET, "sk_test"));

    for (Map<String, String> environment : environments) {
      assertConfigurationError(MISSING_ACCESS_KEY, () -> factory.buildCaptureUrl(environment::get));
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("factories")
  void aWhitespaceOnlyVariableIsAnErrorThatNamesIt(String name, ClientFactory factory) {
    assertConfigurationError(
        "SCREENSHOTSCOUT_ACCESS_KEY is set but blank.",
        () -> factory.buildCaptureUrl(Map.of(ACCESS, "  ")::get));
    assertConfigurationError(
        "SCREENSHOTSCOUT_SECRET_KEY is set but blank.",
        () -> factory.buildCaptureUrl(Map.of(ACCESS, "ak_test", SECRET, " \t")::get));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("factories")
  void anAccessKeyFromTheEnvironmentGetsTheUsualChecks(String name, ClientFactory factory) {
    assertConfigurationError(
        "The Screenshot Scout access key is not a valid Bearer credential.",
        () -> factory.buildCaptureUrl(Map.of(ACCESS, "key with spaces")::get));
  }

  @Test
  void theVariablesAreReadOnceWhenTheClientIsBuilt() throws Exception {
    Map<String, String> environment = new HashMap<>(BOTH_KEYS);
    List<String> names = new ArrayList<>();
    TestHttpClient transport = jsonTransport();
    ScreenshotScoutClient client =
        ScreenshotScoutClient.builder()
            .environment(recording(environment, names))
            .fromEnv()
            .httpClient(transport)
            .build();
    environment.replaceAll((name, value) -> "changed");

    client.capture(TARGET, OPTIONS);

    assertEquals(List.of(ACCESS, SECRET), names);
    HttpRequest request = transport.requests().get(0);
    assertEquals("Bearer ak_test", authorization(request));
    assertEquals(
        List.of(SIGNATURE), TestHttpClient.queryParameters(request.uri()).get("signature"));
  }

  @Test
  void aSecretKeyFromTheEnvironmentSendsASignedGetWhenNoMethodIsChosen() throws Exception {
    TestHttpClient transport = jsonTransport();
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder().environment(BOTH_KEYS::get).fromEnv().httpClient(transport);

    builder.build().capture(TARGET, OPTIONS);
    builder.buildAsync().capture(TARGET, OPTIONS).join();

    assertEquals(2, transport.requests().size());
    for (HttpRequest request : transport.requests()) {
      assertEquals("GET", request.method());
      assertEquals("Bearer ak_test", authorization(request));
      assertEquals(
          List.of(SIGNATURE), TestHttpClient.queryParameters(request.uri()).get("signature"));
    }
  }

  @Test
  void anAccessKeyAloneFromTheEnvironmentSendsPostWhenNoMethodIsChosen() throws Exception {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder()
            .environment(Map.of(ACCESS, "ak_test")::get)
            .fromEnv()
            .httpClient(transport);

    builder.build().capture("https://example.com");
    builder.buildAsync().capture("https://example.com").join();

    assertEquals(2, transport.requests().size());
    for (HttpRequest request : transport.requests()) {
      assertEquals("POST", request.method());
      assertEquals("Bearer ak_test", authorization(request));
    }
  }

  @Test
  void anExplicitPostNamesTheVariableTheSecretKeyCameFrom() {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder().environment(BOTH_KEYS::get).fromEnv().httpClient(transport);
    ScreenshotScoutClient client = builder.build();
    ScreenshotScoutAsyncClient asyncClient = builder.buildAsync();
    String message =
        "POST requests can't be signed. This client has a secret key from"
            + " SCREENSHOTSCOUT_SECRET_KEY: send this capture with method GET, or use a client"
            + " without a secret key and an API key that doesn't require signed requests.";

    assertConfigurationError(
        message, () -> client.capture("https://example.com", null, CaptureHttpMethod.POST));
    assertConfigurationError(
        message, () -> asyncClient.capture("https://example.com", null, CaptureHttpMethod.POST));
    assertTrue(transport.requests().isEmpty());
  }

  @Test
  void fromEnvCantBeCombinedWithAccessKeyOrSecretKey() {
    List<String> names = new ArrayList<>();
    Function<String, String> environment = recording(BOTH_KEYS, names);
    List<ScreenshotScoutClientBuilder> builders =
        List.of(
            ScreenshotScoutClient.builder().environment(environment).fromEnv().accessKey("ak_code"),
            ScreenshotScoutClient.builder().environment(environment).accessKey("ak_code").fromEnv(),
            ScreenshotScoutClient.builder().environment(environment).fromEnv().secretKey("sk_code"),
            ScreenshotScoutClient.builder()
                .environment(environment)
                .secretKey("sk_code")
                .fromEnv());

    for (ScreenshotScoutClientBuilder builder : builders) {
      assertConfigurationError(
          "fromEnv() can't be combined with accessKey() or secretKey().", builder::build);
      assertConfigurationError(
          "fromEnv() can't be combined with accessKey() or secretKey().", builder::buildAsync);
    }
    assertEquals(List.of(), names);
  }

  @Test
  void aBuilderWithoutFromEnvNeverReadsTheEnvironment() throws Exception {
    List<String> names = new ArrayList<>();
    Function<String, String> environment = recording(BOTH_KEYS, names);
    TestHttpClient transport = new TestHttpClient();

    assertConfigurationError(
        "A non-blank Screenshot Scout access key is required.",
        () -> ScreenshotScoutClient.builder().environment(environment).build());
    assertConfigurationError(
        "A non-blank Screenshot Scout access key is required.",
        () ->
            ScreenshotScoutClient.builder()
                .environment(environment)
                .secretKey("sk_code")
                .buildAsync());
    ScreenshotScoutClient.builder()
        .environment(environment)
        .accessKey("ak_code")
        .httpClient(transport)
        .build()
        .capture("https://example.com");

    HttpRequest request = transport.requests().get(0);
    assertEquals("POST", request.method());
    assertEquals("Bearer ak_code", authorization(request));
    assertEquals(List.of(), names);
  }

  private static Function<String, String> recording(
      Map<String, String> values, List<String> names) {
    return name -> {
      names.add(name);
      return values.get(name);
    };
  }

  private static TestHttpClient jsonTransport() {
    TestHttpClient transport = new TestHttpClient();
    transport.respond(
        200,
        Map.of("content-type", List.of("application/json")),
        "{}".getBytes(StandardCharsets.UTF_8));
    return transport;
  }

  private static Map<String, List<String>> query(String url) {
    return TestHttpClient.queryParameters(URI.create(url));
  }

  private static String authorization(HttpRequest request) {
    return request.headers().firstValue("authorization").orElseThrow();
  }

  private static void assertConfigurationError(String message, Executable executable) {
    ScreenshotScoutConfigurationException error =
        assertThrows(ScreenshotScoutConfigurationException.class, executable);
    assertEquals(message, error.getMessage());
  }
}
