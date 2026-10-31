# Examples

These examples receive credentials from the application environment and pass them explicitly to
the SDK. The core SDK never reads environment variables. Each example contains its own credential
setup and does not depend on shared example helper code.

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

`BuildCaptureUrlExample` signs the generated URL when `SCREENSHOTSCOUT_SECRET_KEY` is set.

The async-I/O example waits for the final inline Screenshot Scout response. It is not a background
capture, queued job, callback, or webhook example.
