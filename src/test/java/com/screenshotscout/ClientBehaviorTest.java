package com.screenshotscout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

class ClientBehaviorTest {
  @Test
  void builderValidatesCredentialsAndCreatesBothClientModels() {
    assertThrows(
        ScreenshotScoutConfigurationException.class, () -> ScreenshotScoutClient.builder().build());
    assertThrows(
        ScreenshotScoutConfigurationException.class,
        () -> ScreenshotScoutClient.builder().accessKey("   ").build());
    assertThrows(
        ScreenshotScoutConfigurationException.class,
        () -> ScreenshotScoutClient.builder().accessKey("key with spaces").build());
    assertThrows(
        ScreenshotScoutConfigurationException.class,
        () -> ScreenshotScoutClient.builder().accessKey("key").secretKey("").build());

    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder().accessKey("ak_test+/=").httpClient(transport);
    assertNotNull(builder.build());
    assertNotNull(builder.buildAsync());
  }

  @Test
  void openServiceValuesRemainRawAndMethodSelectorIsClosed() {
    assertEquals("webp", CaptureFormat.WEBP.value());
    assertEquals("future-format", CaptureFormat.fromString("future-format").value());
    assertEquals(
        CaptureFormat.fromString("future-format"), CaptureFormat.fromString("future-format"));
    assertFalse(CaptureFormat.WEBP.equals(CaptureResponseType.fromString("webp")));
    assertEquals("GET", CaptureHttpMethod.GET.name());
    assertEquals("POST", CaptureHttpMethod.POST.name());
  }

  @Test
  void blockingTransportFailuresRetainCauseAndAreNotRetried() {
    IOException nativeCause = new IOException("socket closed");
    TestHttpClient transport = new TestHttpClient();
    transport.fail(nativeCause);
    ScreenshotScoutClient client = client(transport);

    ScreenshotScoutTransportException error =
        assertThrows(
            ScreenshotScoutTransportException.class, () -> client.capture("https://example.com"));

    assertSame(nativeCause, error.getCause());
    assertEquals(1, transport.blockingCalls());
  }

  @Test
  void blockingThreadInterruptionIsPreservedAndDoesNotCallTransport() {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClient client = client(transport);
    Thread.currentThread().interrupt();
    try {
      assertThrows(InterruptedException.class, () -> client.capture("https://example.com"));
      assertTrue(Thread.currentThread().isInterrupted());
      assertEquals(0, transport.blockingCalls());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  void asyncClientWaitsForTheFinalInlineResponse() {
    TestHttpClient transport = new TestHttpClient();
    CompletableFuture<HttpResponse<byte[]>> pending = transport.usePendingAsyncResponse();
    ScreenshotScoutAsyncClient client = asyncClient(transport);

    CompletableFuture<CaptureResponse> capture = client.capture("https://example.com");

    assertFalse(capture.isDone());
    assertEquals(1, transport.asyncCalls());
    pending.complete(transport.responseFor(transport.requests().get(0)));
    assertInstanceOf(BinaryCaptureResponse.class, capture.join());
    assertTrue(capture.isDone());
  }

  @Test
  void cancellingAsyncResultCancelsTheTransportFuture() {
    TestHttpClient transport = new TestHttpClient();
    CompletableFuture<HttpResponse<byte[]>> pending = transport.usePendingAsyncResponse();
    ScreenshotScoutAsyncClient client = asyncClient(transport);

    CompletableFuture<CaptureResponse> capture = client.capture("https://example.com");

    assertTrue(capture.cancel(true));
    assertTrue(capture.isCancelled());
    assertTrue(pending.isCancelled());
    assertThrows(CancellationException.class, capture::join);
  }

  @Test
  void asyncTransportFailuresRetainCauseAndAreNotRetried() {
    IOException nativeCause = new IOException("connection reset");
    TestHttpClient transport = new TestHttpClient();
    transport.fail(nativeCause);
    ScreenshotScoutAsyncClient client = asyncClient(transport);

    CompletionException completion =
        assertThrows(CompletionException.class, () -> client.capture("https://example.com").join());

    ScreenshotScoutTransportException error =
        assertInstanceOf(ScreenshotScoutTransportException.class, completion.getCause());
    assertSame(nativeCause, error.getCause());
    assertEquals(1, transport.asyncCalls());
  }

  @Test
  void injectedTransportIsSharedWithoutAddingARequestTimeout() throws Exception {
    TestHttpClient transport = new TestHttpClient();
    ScreenshotScoutClientBuilder builder =
        ScreenshotScoutClient.builder().accessKey("key").httpClient(transport);
    ScreenshotScoutClient blocking = builder.build();
    ScreenshotScoutAsyncClient asynchronous = builder.buildAsync();

    blocking.capture("https://example.com");
    asynchronous.capture("https://example.com").join();

    assertEquals(2, transport.requests().size());
    assertTrue(transport.requests().stream().allMatch(request -> request.timeout().isEmpty()));
  }

  private static ScreenshotScoutClient client(TestHttpClient transport) {
    return ScreenshotScoutClient.builder().accessKey("key").httpClient(transport).build();
  }

  private static ScreenshotScoutAsyncClient asyncClient(TestHttpClient transport) {
    return ScreenshotScoutAsyncClient.builder().accessKey("key").httpClient(transport).buildAsync();
  }
}
