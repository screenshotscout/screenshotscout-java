package com.screenshotscout.examples;

import com.screenshotscout.BinaryCaptureResponse;
import com.screenshotscout.CaptureFormat;
import com.screenshotscout.CaptureOptions;
import com.screenshotscout.CaptureResponse;
import com.screenshotscout.ScreenshotScoutClient;
import java.nio.file.Files;
import java.nio.file.Path;

/** Renders an invoice written as HTML to a PDF. */
public final class CaptureFromHtmlExample {
  private CaptureFromHtmlExample() {}

  public static void main(String[] args) throws Exception {
    String html =
        """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8" />
            <title>Invoice #1042</title>
            <style>
              body { font-family: system-ui, sans-serif; margin: 48px; color: #111; }
              table { width: 100%; border-collapse: collapse; margin-top: 24px; }
              th, td { text-align: left; padding: 8px 0; border-bottom: 1px solid #ddd; }
              .total td { font-weight: 600; }
            </style>
          </head>
          <body>
            <h1>Invoice #1042</h1>
            <p>Billed to Example Corp.</p>
            <table>
              <tr><th>Item</th><th>Amount</th></tr>
              <tr><td>Consulting, 4 hours</td><td>$480.00</td></tr>
              <tr class="total"><td>Total</td><td>$480.00</td></tr>
            </table>
          </body>
        </html>
        """;

    // Reads SCREENSHOTSCOUT_ACCESS_KEY
    ScreenshotScoutClient client = ScreenshotScoutClient.fromEnv();

    CaptureResponse response =
        client.captureFromHtml(html, CaptureOptions.builder().format(CaptureFormat.PDF).build());
    BinaryCaptureResponse binary = (BinaryCaptureResponse) response;
    Files.write(Path.of("invoice.pdf"), binary.bytes());
    binary.screenshotUrl().ifPresent(System.out::println);
  }
}
