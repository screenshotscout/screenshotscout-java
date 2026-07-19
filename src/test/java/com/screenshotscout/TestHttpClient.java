package com.screenshotscout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

final class TestHttpClient extends HttpClient {
  private final List<HttpRequest> requests = new ArrayList<>();
  private ResponseSpec response =
      new ResponseSpec(200, Map.of("content-type", List.of("image/png")), new byte[] {1});
  private IOException failure;
  private CompletableFuture<HttpResponse<byte[]>> pendingBlockingResponse;
  private CompletableFuture<HttpRequest> pendingBlockingRequest;
  private CompletableFuture<HttpResponse<byte[]>> pendingAsyncResponse;
  private int blockingCalls;
  private int asyncCalls;

  void respond(int status, Map<String, List<String>> headers, byte[] body) {
    response = new ResponseSpec(status, headers, body.clone());
    failure = null;
    pendingBlockingResponse = null;
    pendingBlockingRequest = null;
    pendingAsyncResponse = null;
  }

  void fail(IOException value) {
    failure = value;
    pendingBlockingResponse = null;
    pendingBlockingRequest = null;
    pendingAsyncResponse = null;
  }

  CompletableFuture<HttpResponse<byte[]>> usePendingBlockingResponse() {
    pendingBlockingResponse = new CompletableFuture<>();
    pendingBlockingRequest = new CompletableFuture<>();
    return pendingBlockingResponse;
  }

  CompletableFuture<HttpRequest> pendingBlockingRequest() {
    if (pendingBlockingRequest == null) {
      throw new IllegalStateException("A pending blocking response has not been configured.");
    }
    return pendingBlockingRequest;
  }

  CompletableFuture<HttpResponse<byte[]>> usePendingAsyncResponse() {
    pendingAsyncResponse = new CompletableFuture<>();
    return pendingAsyncResponse;
  }

  HttpResponse<byte[]> responseFor(HttpRequest request) {
    return response.toResponse(request);
  }

  List<HttpRequest> requests() {
    return Collections.unmodifiableList(requests);
  }

  int blockingCalls() {
    return blockingCalls;
  }

  int asyncCalls() {
    return asyncCalls;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> HttpResponse<T> send(
      HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
      throws IOException, InterruptedException {
    blockingCalls++;
    requests.add(request);
    if (failure != null) {
      throw failure;
    }
    if (pendingBlockingResponse != null) {
      pendingBlockingRequest.complete(request);
      try {
        return (HttpResponse<T>) pendingBlockingResponse.get();
      } catch (ExecutionException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof IOException ioException) {
          throw ioException;
        }
        if (cause instanceof RuntimeException runtimeException) {
          throw runtimeException;
        }
        throw new IOException("The pending test response failed.", cause);
      }
    }
    return (HttpResponse<T>) response.toResponse(request);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> CompletableFuture<HttpResponse<T>> sendAsync(
      HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
    asyncCalls++;
    requests.add(request);
    CompletableFuture<HttpResponse<byte[]>> selected;
    if (failure != null) {
      selected = CompletableFuture.failedFuture(failure);
    } else if (pendingAsyncResponse != null) {
      selected = pendingAsyncResponse;
    } else {
      selected = CompletableFuture.completedFuture(response.toResponse(request));
    }
    return (CompletableFuture<HttpResponse<T>>) (CompletableFuture<?>) selected;
  }

  @Override
  public <T> CompletableFuture<HttpResponse<T>> sendAsync(
      HttpRequest request,
      HttpResponse.BodyHandler<T> responseBodyHandler,
      HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
    return sendAsync(request, responseBodyHandler);
  }

  @Override
  public Optional<CookieHandler> cookieHandler() {
    return Optional.empty();
  }

  @Override
  public Optional<Duration> connectTimeout() {
    return Optional.empty();
  }

  @Override
  public Redirect followRedirects() {
    return Redirect.NEVER;
  }

  @Override
  public Optional<ProxySelector> proxy() {
    return Optional.empty();
  }

  @Override
  public SSLContext sslContext() {
    try {
      return SSLContext.getDefault();
    } catch (Exception exception) {
      throw new IllegalStateException(exception);
    }
  }

  @Override
  public SSLParameters sslParameters() {
    return new SSLParameters();
  }

  @Override
  public Optional<Authenticator> authenticator() {
    return Optional.empty();
  }

  @Override
  public Version version() {
    return Version.HTTP_2;
  }

  @Override
  public Optional<Executor> executor() {
    return Optional.empty();
  }

  static byte[] requestBody(HttpRequest request) {
    HttpRequest.BodyPublisher publisher = request.bodyPublisher().orElseThrow();
    CompletableFuture<byte[]> result = new CompletableFuture<>();
    publisher.subscribe(
        new Flow.Subscriber<>() {
          private final ByteArrayOutputStream output = new ByteArrayOutputStream();

          @Override
          public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
          }

          @Override
          public void onNext(ByteBuffer item) {
            byte[] chunk = new byte[item.remaining()];
            item.get(chunk);
            output.writeBytes(chunk);
          }

          @Override
          public void onError(Throwable throwable) {
            result.completeExceptionally(throwable);
          }

          @Override
          public void onComplete() {
            result.complete(output.toByteArray());
          }
        });
    return result.join();
  }

  static Map<String, List<String>> queryParameters(URI uri) {
    Map<String, List<String>> result = new LinkedHashMap<>();
    String query = uri.getRawQuery();
    if (query == null || query.isEmpty()) {
      return result;
    }
    for (String pair : query.split("&", -1)) {
      int separator = pair.indexOf('=');
      String rawName = separator < 0 ? pair : pair.substring(0, separator);
      String rawValue = separator < 0 ? "" : pair.substring(separator + 1);
      String name = java.net.URLDecoder.decode(rawName, java.nio.charset.StandardCharsets.UTF_8);
      String value = java.net.URLDecoder.decode(rawValue, java.nio.charset.StandardCharsets.UTF_8);
      result.computeIfAbsent(name, ignored -> new ArrayList<>()).add(value);
    }
    return result;
  }

  private record ResponseSpec(int status, Map<String, List<String>> headers, byte[] body) {
    private HttpResponse<byte[]> toResponse(HttpRequest request) {
      return new TestResponse(
          status, request, HttpHeaders.of(headers, (name, value) -> true), body.clone());
    }
  }

  private record TestResponse(int statusCode, HttpRequest request, HttpHeaders headers, byte[] body)
      implements HttpResponse<byte[]> {
    @Override
    public Optional<HttpResponse<byte[]>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public Optional<SSLSession> sslSession() {
      return Optional.empty();
    }

    @Override
    public URI uri() {
      return request.uri();
    }

    @Override
    public HttpClient.Version version() {
      return HttpClient.Version.HTTP_2;
    }
  }
}
