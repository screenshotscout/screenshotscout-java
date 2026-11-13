package com.screenshotscout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
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
  private static final String BASE_URL = "SCREENSHOTSCOUT_BASE_URL";
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

  @ParameterizedTest(name = "{0}")
  @MethodSource("factories")
  void theBaseUrlVariableIsUsedWhenNoBaseUrlIsPassed(String name, ClientFactory factory) {
    String url =
        factory.buildCaptureUrl(
            Map.of(ACCESS, "ak_test", SECRET, "sk_test", BASE_URL, "http://localhost:8787/")::get);

    assertEquals("http://localhost:8787/v1/capture", endpoint(url));
    assertEquals(List.of("ak_test"), query(url).get("access_key"));
    assertEquals(List.of(SIGNATURE), query(url).get("signature"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("factories")
  void anEmptyBaseUrlVariableMeansTheApiAndABlankOneIsAnError(String name, ClientFactory factory) {
    String url = factory.buildCaptureUrl(Map.of(ACCESS, "ak_test", BASE_URL, "")::get);

    assertEquals("https://api.screenshotscout.com/v1/capture", endpoint(url));
    assertConfigurationError(
        "SCREENSHOTSCOUT_BASE_URL is set but blank.",
        () -> factory.buildCaptureUrl(Map.of(ACCESS, "ak_test", BASE_URL, " \t")::get));
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

    assertEquals(List.of(ACCESS, SECRET, BASE_URL), names);
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

  @Test
  void theBaseUrlVariableIsReadOnceWhenTheClientIsBuilt() throws Exception {
    Map<String, String> environment =
        new HashMap<>(Map.of(ACCESS, "ak_test", BASE_URL, "http://localhost:8787"));
    List<String> names = new ArrayList<>();
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder()
            .environment(recording(environment, names))
            .fromEnv()
            .httpClient(transport);
    ScreenshotScoutClient client = builder.build();
    ScreenshotScoutAsyncClient asyncClient = builder.buildAsync();
    environment.put(BASE_URL, "http://changed.example");

    client.capture("https://example.com");
    asyncClient.capture("https://example.com").join();

    assertEquals(List.of(ACCESS, SECRET, BASE_URL, ACCESS, SECRET, BASE_URL), names);
    assertEquals(2, transport.requests().size());
    for (HttpRequest request : transport.requests()) {
      assertEquals("POST", request.method());
      assertEquals("http://localhost:8787/v1/capture", request.uri().toString());
    }
    assertEquals(
        "http://localhost:8787/v1/capture?access_key=ak_test&url=https%3A%2F%2Fexample.com",
        client.buildCaptureUrl("https://example.com"));
  }

  @Test
  void anEmptyOrNullBaseUrlInCodeLetsFromEnvReadTheVariable() {
    for (String baseUrl : new String[] {"", null}) {
      List<String> names = new ArrayList<>();
      ScreenshotScoutClient client =
          ScreenshotScoutClient.builder()
              .environment(
                  recording(Map.of(ACCESS, "ak_test", BASE_URL, "http://localhost:8787"), names))
              .fromEnv()
              .baseUrl(baseUrl)
              .httpClient(new TestHttpClient())
              .build();

      assertEquals(
          "http://localhost:8787/v1/capture",
          endpoint(client.buildCaptureUrl("https://example.com")));
      assertEquals(List.of(ACCESS, SECRET, BASE_URL), names);
    }
  }

  @Test
  void aBaseUrlPassedInCodeWinsOverTheVariable() throws Exception {
    List<String> names = new ArrayList<>();
    Function<String, String> environment =
        recording(Map.of(ACCESS, "ak_test", BASE_URL, "http://localhost:8787"), names);
    TestHttpClient transport = new TestHttpClient();
    List<ScreenshotScoutClientBuilder> builders =
        List.of(
            ScreenshotScoutClient.builder()
                .environment(environment)
                .fromEnv()
                .baseUrl("https://proxy.example.com"),
            ScreenshotScoutClient.builder()
                .environment(environment)
                .baseUrl("https://proxy.example.com")
                .fromEnv());

    for (ScreenshotScoutClientBuilder builder : builders) {
      builder.httpClient(transport);
      builder.build().capture("https://example.com");
      builder.buildAsync().capture("https://example.com").join();
    }

    assertEquals(4, transport.requests().size());
    for (HttpRequest request : transport.requests()) {
      assertEquals("https://proxy.example.com/v1/capture", request.uri().toString());
      assertEquals("Bearer ak_test", authorization(request));
    }
    assertFalse(names.contains(BASE_URL));
  }

  @Test
  void aBuilderWithKeysInCodeIgnoresTheBaseUrlVariable() throws Exception {
    List<String> names = new ArrayList<>();
    Function<String, String> environment =
        recording(
            Map.of(ACCESS, "ak_test", SECRET, "sk_test", BASE_URL, "http://localhost:8787"), names);
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder()
            .environment(environment)
            .accessKey("ak_code")
            .httpClient(transport);
    ScreenshotScoutClient client = builder.build();

    client.capture("https://example.com");
    builder.buildAsync().capture("https://example.com").join();

    assertEquals(2, transport.requests().size());
    for (HttpRequest request : transport.requests()) {
      assertEquals("https://api.screenshotscout.com/v1/capture", request.uri().toString());
    }
    assertEquals(
        "https://api.screenshotscout.com/v1/capture?access_key=ak_code&url=https%3A%2F%2Fexample.com",
        client.buildCaptureUrl("https://example.com"));
    assertEquals(List.of(), names);
  }

  @Test
  void transportFailuresNameTheBaseUrlVariableOnlyWhenTheBaseUrlCameFromIt() {
    String message =
        "Screenshot Scout request failed before a complete HTTP response body was received.";
    Map<String, String> environment = Map.of(ACCESS, "ak_test", BASE_URL, "http://localhost:8787");

    assertTransportFailures(
        message + " The base URL comes from SCREENSHOTSCOUT_BASE_URL.",
        ScreenshotScoutClient.builder().environment(environment::get).fromEnv());
    assertTransportFailures(
        message,
        ScreenshotScoutClient.builder()
            .environment(environment::get)
            .fromEnv()
            .baseUrl("http://localhost:8787"));
    assertTransportFailures(
        message,
        ScreenshotScoutClient.builder().environment(environment::get).accessKey("ak_test"));
    assertTransportFailures(
        message,
        ScreenshotScoutClient.builder().environment(Map.of(ACCESS, "ak_test")::get).fromEnv());
  }

  @Test
  void aRequestThatCantBeConstructedNamesTheBaseUrlVariableOnlyWhenTheBaseUrlCameFromIt() {
    String message = "The Screenshot Scout HTTP request could not be constructed.";
    TestHttpClient transport = new TestHttpClient();

    assertConstructionFailures(
        message + " The base URL comes from SCREENSHOTSCOUT_BASE_URL.",
        ScreenshotScoutClient.builder()
            .environment(Map.of(ACCESS, "ak_test", BASE_URL, "not a url")::get)
            .fromEnv()
            .httpClient(transport));
    assertConstructionFailures(
        message,
        ScreenshotScoutClient.builder()
            .environment(Map.of(ACCESS, "ak_test", BASE_URL, "http://localhost:8787")::get)
            .fromEnv()
            .baseUrl("not a url")
            .httpClient(transport));
    assertTrue(transport.requests().isEmpty());
  }

  @Test
  void cancellationIsUnchangedWhenTheBaseUrlComesFromTheVariable() throws Exception {
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder()
            .environment(Map.of(ACCESS, "ak_test", BASE_URL, "http://localhost:8787")::get)
            .fromEnv();

    TestHttpClient transport = new TestHttpClient();
    CompletableFuture<HttpResponse<byte[]>> pending = transport.usePendingAsyncResponse();
    CompletableFuture<CaptureResponse> capture =
        builder.httpClient(transport).buildAsync().capture("https://example.com");
    assertTrue(capture.cancel(true));
    assertTrue(pending.isCancelled());
    assertThrows(CancellationException.class, capture::join);

    TestHttpClient cancellingTransport = new TestHttpClient();
    CompletableFuture<HttpResponse<byte[]>> cancelled =
        cancellingTransport.usePendingAsyncResponse();
    CompletableFuture<CaptureResponse> cancelledCapture =
        builder.httpClient(cancellingTransport).buildAsync().capture("https://example.com");
    assertTrue(cancelled.cancel(false));
    assertTrue(cancelledCapture.isCancelled());
    assertThrows(CancellationException.class, cancelledCapture::join);

    TestHttpClient blockingTransport = new TestHttpClient();
    blockingTransport.usePendingBlockingResponse();
    ScreenshotScoutClient client = builder.httpClient(blockingTransport).build();
    CompletableFuture<Throwable> thrown = new CompletableFuture<>();
    Thread thread =
        new Thread(
            () -> {
              try {
                client.capture("https://example.com");
                thrown.complete(null);
              } catch (Throwable error) {
                thrown.complete(error);
              }
            });
    thread.start();
    blockingTransport.pendingBlockingRequest().get(5, TimeUnit.SECONDS);
    thread.interrupt();
    assertInstanceOf(InterruptedException.class, thrown.get(5, TimeUnit.SECONDS));
    thread.join(TimeUnit.SECONDS.toMillis(5));
  }

  /** Checks the blocking failure and both async failures of clients from the builder. */
  private static void assertTransportFailures(
      String message, ScreenshotScoutClientBuilder builder) {
    IOException ioFailure = new IOException("connection refused");
    TestHttpClient failingTransport = new TestHttpClient();
    failingTransport.fail(ioFailure);
    builder.httpClient(failingTransport);
    ScreenshotScoutClient client = builder.build();
    ScreenshotScoutAsyncClient asyncClient = builder.buildAsync();

    ScreenshotScoutTransportException blockingError =
        assertThrows(
            ScreenshotScoutTransportException.class, () -> client.capture("https://example.com"));
    assertEquals(message, blockingError.getMessage());
    assertSame(ioFailure, blockingError.getCause());

    CompletionException completion =
        assertThrows(
            CompletionException.class, () -> asyncClient.capture("https://example.com").join());
    ScreenshotScoutTransportException asyncError =
        assertInstanceOf(ScreenshotScoutTransportException.class, completion.getCause());
    assertEquals(message, asyncError.getMessage());
    assertSame(ioFailure, asyncError.getCause());

    IllegalStateException rejection = new IllegalStateException("rejected");
    TestHttpClient rejectingTransport = new TestHttpClient();
    rejectingTransport.throwFromSendAsync(rejection);
    CompletableFuture<CaptureResponse> rejected =
        builder.httpClient(rejectingTransport).buildAsync().capture("https://example.com");
    CompletionException rejectedCompletion =
        assertThrows(CompletionException.class, rejected::join);
    ScreenshotScoutTransportException rejectedError =
        assertInstanceOf(ScreenshotScoutTransportException.class, rejectedCompletion.getCause());
    assertEquals(message, rejectedError.getMessage());
    assertSame(rejection, rejectedError.getCause());
  }

  /** Checks that every capture from clients built by the builder fails before it's sent. */
  private static void assertConstructionFailures(
      String message, ScreenshotScoutClientBuilder builder) {
    ScreenshotScoutClient client = builder.build();
    ScreenshotScoutAsyncClient asyncClient = builder.buildAsync();
    List<Executable> captures =
        List.of(
            () -> client.capture("https://example.com"),
            () -> client.capture("https://example.com", null, CaptureHttpMethod.GET),
            () -> asyncClient.capture("https://example.com"),
            () -> asyncClient.capture("https://example.com", null, CaptureHttpMethod.GET));

    for (Executable capture : captures) {
      ScreenshotScoutSerializationException error =
          assertThrows(ScreenshotScoutSerializationException.class, capture);
      assertEquals(message, error.getMessage());
      assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }
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

  /** Returns the part of a capture URL before its query. */
  private static String endpoint(String url) {
    return url.substring(0, url.indexOf('?'));
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
