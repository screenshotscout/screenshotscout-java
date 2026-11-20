# Examples

Each example creates its own client with `fromEnv()` and does not depend on shared example helper
code.

From the repository root, install the local package and compile the examples:

```shell
mvn install
mvn -f examples/pom.xml package
```

Set `SCREENSHOTSCOUT_ACCESS_KEY`, then run one of these classes with the example project's Maven
classpath:

- `com.screenshotscout.examples.BinaryCaptureExample`
- `com.screenshotscout.examples.JsonCaptureExample`
- `com.screenshotscout.examples.AsyncIoCaptureExample`
- `com.screenshotscout.examples.BuildCaptureUrlExample`
- `com.screenshotscout.examples.CaptureFromHtmlExample`

If `SCREENSHOTSCOUT_SECRET_KEY` is also set, the clients sign their requests and the URL that
`BuildCaptureUrlExample` prints. `CaptureFromHtmlExample` renders an invoice to PDF and sends the
HTML in a POST request, which can't be signed, so run it without `SCREENSHOTSCOUT_SECRET_KEY`.

The async-I/O example waits for the final inline Screenshot Scout response. It is not a background
capture, queued job, callback, or webhook example.
