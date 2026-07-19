# Screenshot Scout Java SDK

The official Java SDK for [Screenshot Scout](https://screenshotscout.com).

> Status: initial local scaffold. The API client is not implemented or published yet.

The library compiles for Java 17 and uses the Maven coordinates
`com.screenshotscout:screenshotscout`.

## Repository layout

- `src/main/java/com/screenshotscout` contains the SDK package.
- `src/test/java/com/screenshotscout` contains focused SDK tests.
- `examples` will contain runnable usage examples when the client is implemented.

## Development

Run the local checks from the repository root with Java 17 or newer and Maven:

```shell
mvn spotless:check
mvn test
mvn package
```

## License

Licensed under the [MIT License](LICENSE).
