# Screenshot Scout Java SDK

The official Java SDK for the [Screenshot Scout](https://screenshotscout.com/) screenshot API.

Capture website screenshots from Java applications.

## Requirements

- Java 17 or newer
- Maven, Gradle, or another Maven Central-compatible build tool

## Installation

Add the SDK to your project:

```xml
<dependency>
  <groupId>com.screenshotscout</groupId>
  <artifactId>screenshotscout</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Get your API credentials

[Sign up for Screenshot Scout](https://screenshotscout.com/auth/signup) or sign in, then copy your
access key and secret key from the [API Keys page](https://screenshotscout.com/app/api-keys). Store
both securely. The access key is required. The secret key is optional and enables
[signed requests](#signed-requests).

## Capture a screenshot

```java
import com.screenshotscout.BinaryCaptureResponse;
import com.screenshotscout.CaptureOptions;
import com.screenshotscout.CaptureResponse;
import com.screenshotscout.ScreenshotScoutClient;
import java.nio.file.Files;
import java.nio.file.Path;

ScreenshotScoutClient client =
    ScreenshotScoutClient.builder()
        .accessKey("YOUR_ACCESS_KEY")
        .build();

CaptureResponse response =
    client.capture(
        "https://example.com",
        CaptureOptions.builder().fullPage(true).build());

if (response instanceof BinaryCaptureResponse binary) {
  Files.write(Path.of("screenshot.png"), binary.bytes());
  binary.screenshotUrl().ifPresent(System.out::println);
}
```

POST is used by default. Without `responseType`, `capture` returns a `BinaryCaptureResponse`.

## Request a JSON result

```java
import com.screenshotscout.CaptureOptions;
import com.screenshotscout.CaptureResponseType;
import com.screenshotscout.JsonCaptureResponse;

CaptureOptions options =
    CaptureOptions.builder()
        .responseType(CaptureResponseType.JSON)
        .build();

JsonCaptureResponse response =
    (JsonCaptureResponse) client.capture("https://example.com", options);

response.result().screenshotUrl().ifPresent(System.out::println);
```

Set `responseType` to `CaptureResponseType.JSON` to receive screenshot metadata instead of binary
file data. Unrecognized result fields are available through `CaptureResult.additionalFields()`.

## Non-blocking client

Use `ScreenshotScoutAsyncClient` when your application needs non-blocking I/O. Its `capture`
methods return a `CompletableFuture` that completes with the final capture response. This does not
create a queued capture job or use webhooks.

```java
import com.screenshotscout.BinaryCaptureResponse;
import com.screenshotscout.ScreenshotScoutAsyncClient;
import java.util.concurrent.CompletableFuture;

ScreenshotScoutAsyncClient asyncClient =
    ScreenshotScoutAsyncClient.builder()
        .accessKey("YOUR_ACCESS_KEY")
        .buildAsync();

CompletableFuture<Void> finished =
    asyncClient
        .capture("https://example.com")
        .thenAccept(
            response -> {
              BinaryCaptureResponse binary = (BinaryCaptureResponse) response;
              System.out.println(binary.bytes().length);
            });

finished.join();
```

Cancel the returned future to cancel an in-flight request.

## Use GET

POST is used by default. Pass `CaptureHttpMethod.GET` to send a GET request:

```java
import com.screenshotscout.CaptureFormat;
import com.screenshotscout.CaptureHttpMethod;
import com.screenshotscout.CaptureOptions;

client.capture(
    "https://example.com",
    CaptureOptions.builder().format(CaptureFormat.WEBP).build(),
    CaptureHttpMethod.GET);
```

## Build a capture URL

Use `buildCaptureUrl` when a browser, an HTML `<img>` element, or another application needs to load
the screenshot directly. It creates a GET capture URL without making an HTTP request.

```java
String captureUrl =
    client.buildCaptureUrl(
        "https://example.com",
        CaptureOptions.builder()
            .fullPage(true)
            .blockAds(true)
            .build());
```

The generated URL contains the access key. A configured secret key signs it automatically;
without a secret key it is unsigned. Treat capture URLs as sensitive. Before exposing them to a
browser or user, configure a secret key and enable **Require signed requests** on the
[API Keys page](https://screenshotscout.com/app/api-keys).

## Signed requests

```java
ScreenshotScoutClient signedClient =
    ScreenshotScoutClient.builder()
        .accessKey("YOUR_ACCESS_KEY")
        .secretKey("YOUR_SECRET_KEY")
        .build();
```

When a secret key is configured, the client automatically signs GET and POST requests and generated
capture URLs. The secret key stays in your application and is never transmitted. See the
[signed requests guide](https://screenshotscout.com/docs/signed-requests).

## Capture options

The target URL is the first `capture` or `buildCaptureUrl` argument. Configure the screenshot with
`CaptureOptions.builder()`:

- Output: `format`, `responseType`
- Network and location: `country`, `proxy`, `geolocationLatitude`, `geolocationLongitude`,
  `geolocationAccuracy`
- Cookies and webpage headers: `cookies`, `headers`
- Timing: `timeout`, `waitUntil`, `navigationTimeout`, `delay`
- Device emulation: `device`, `deviceViewportWidth`, `deviceViewportHeight`,
  `deviceScaleFactor`, `deviceIsMobile`, `deviceHasTouch`, `deviceUserAgent`
- Page behavior: `timezone`, `mediaType`, `colorScheme`, `reducedMotion`
- Full page: `fullPage`, `fullPagePreScroll`, `fullPagePreScrollStep`,
  `fullPagePreScrollStepDelay`, `fullPageMaxHeight`
- Blocking: `blockCookieBanners`, `blockAds`, `blockChatWidgets`
- DOM changes: `hideSelectors`, `clickSelectors`, `clickAllSelectors`, `injectCss`, `injectJs`,
  `bypassCsp`
- Framing: `selector`, `clipX`, `clipY`, `clipWidth`, `clipHeight`
- Image output: `imageWidth`, `imageHeight`, `imageMode`, `imageAnchor`,
  `imageAllowUpscale`, `imageBackground`, `imageQuality`
- PDF: `pdfPaperFormat`, `pdfLandscape`, `pdfPrintBackground`, `pdfMargin`, `pdfMarginTop`,
  `pdfMarginRight`, `pdfMarginBottom`, `pdfMarginLeft`, `pdfScale`
- Caching: `cache`, `cacheTtl`, `cacheKey`
- Storage: `storageMode`, `storageEndpoint`, `storageBucket`, `storageRegion`,
  `storageObjectKey`

Use the provided constants for documented option values:

```java
CaptureOptions options =
    CaptureOptions.builder()
        .format(CaptureFormat.WEBP)
        .waitUntil(CaptureWaitUntil.LOAD)
        .build();
```

If Screenshot Scout supports a value that is not yet available as a constant, use the value
type's `fromString` method. See the
[Screenshot Scout option reference](https://screenshotscout.com/docs/screenshot-options) for
available values and examples.

## Timeouts, interruption, and cancellation

`CaptureOptions.timeout` is the Screenshot Scout service-side capture budget in seconds. It is
not an HTTP client deadline.

The blocking `capture` methods declare `InterruptedException`. For non-blocking calls, cancel the
returned `CompletableFuture` when your application no longer needs the request.

## Advanced: custom JDK HttpClient

Most applications can use the default HTTP client. Inject an `HttpClient` when you need custom
connection, proxy, redirect, authentication, or executor settings:

```java
import java.net.http.HttpClient;
import java.time.Duration;

HttpClient transport =
    HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

ScreenshotScoutClient client =
    ScreenshotScoutClient.builder()
        .accessKey("YOUR_ACCESS_KEY")
        .httpClient(transport)
        .build();
```

The SDK uses the injected `HttpClient` with its existing configuration.

## Raw responses and errors

Every successful response exposes `rawResponse()` with the HTTP status, headers, content type, and
body. `ScreenshotScoutApiException` provides the same response details for API errors.

```java
try {
  client.capture("https://example.com");
} catch (ScreenshotScoutApiException error) {
  System.err.println(error.statusCode());
  error.errorCode().ifPresent(System.err::println);
  error.errorMessage().ifPresent(System.err::println);
  System.err.println(error.responseBody());
  System.err.println(error.rawResponse().headers().map());
} catch (ScreenshotScoutTransportException error) {
  error.getCause().printStackTrace();
}
```

The five SDK failure categories are:

- `ScreenshotScoutConfigurationException`
- `ScreenshotScoutSerializationException`
- `ScreenshotScoutTransportException`
- `ScreenshotScoutApiException`
- `ScreenshotScoutResponseDecodingException`

All extend `ScreenshotScoutException`. Failed requests are not retried automatically.

## Development

Run the local checks with Java 17 or newer and Maven:

```shell
mvn spotless:check
mvn test
mvn package
```

## License

Licensed under the [MIT License](LICENSE).
