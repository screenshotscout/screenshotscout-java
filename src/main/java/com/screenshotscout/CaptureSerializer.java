package com.screenshotscout;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CaptureSerializer {
  private CaptureSerializer() {}

  static SerializedCaptureOptions serialize(String targetUrl, CaptureOptions options) {
    if (targetUrl == null) {
      throw new ScreenshotScoutSerializationException(
          "The capture target URL must be a string.", "url");
    }

    SerializationBuilder serialized = new SerializationBuilder(targetUrl);
    if (options == null) {
      return serialized.build();
    }

    serialized.add("format", options.format());
    serialized.add("response_type", options.responseType());
    serialized.add("country", options.country());
    serialized.add("proxy", options.proxy());
    serialized.add("geolocation_latitude", options.geolocationLatitude());
    serialized.add("geolocation_longitude", options.geolocationLongitude());
    serialized.add("geolocation_accuracy", options.geolocationAccuracy());
    serialized.addRepeated("cookies", "cookies", options.cookies());
    serialized.addRepeated("headers", "headers", options.headers());
    serialized.add("timeout", options.timeout());
    serialized.add("wait_until", options.waitUntil());
    serialized.add("navigation_timeout", options.navigationTimeout());
    serialized.add("delay", options.delay());
    serialized.add("device", options.device());
    serialized.add("device_viewport_width", options.deviceViewportWidth());
    serialized.add("device_viewport_height", options.deviceViewportHeight());
    serialized.add("device_scale_factor", options.deviceScaleFactor());
    serialized.add("device_is_mobile", options.deviceIsMobile());
    serialized.add("device_has_touch", options.deviceHasTouch());
    serialized.add("device_user_agent", options.deviceUserAgent());
    serialized.add("timezone", options.timezone());
    serialized.add("media_type", options.mediaType());
    serialized.add("color_scheme", options.colorScheme());
    serialized.add("reduced_motion", options.reducedMotion());
    serialized.add("full_page", options.fullPage());
    serialized.add("full_page_pre_scroll", options.fullPagePreScroll());
    serialized.add("full_page_pre_scroll_step", options.fullPagePreScrollStep());
    serialized.add("full_page_pre_scroll_step_delay", options.fullPagePreScrollStepDelay());
    serialized.add("full_page_max_height", options.fullPageMaxHeight());
    serialized.add("block_cookie_banners", options.blockCookieBanners());
    serialized.add("block_ads", options.blockAds());
    serialized.add("block_chat_widgets", options.blockChatWidgets());
    serialized.addRepeated("hideSelectors", "hide_selectors", options.hideSelectors());
    serialized.addRepeated("clickSelectors", "click_selectors", options.clickSelectors());
    serialized.addRepeated("clickAllSelectors", "click_all_selectors", options.clickAllSelectors());
    serialized.addRepeated("injectCss", "inject_css", options.injectCss());
    serialized.addRepeated("injectJs", "inject_js", options.injectJs());
    serialized.add("bypass_csp", options.bypassCsp());
    serialized.add("selector", options.selector());
    serialized.add("clip_x", options.clipX());
    serialized.add("clip_y", options.clipY());
    serialized.add("clip_width", options.clipWidth());
    serialized.add("clip_height", options.clipHeight());
    serialized.add("image_width", options.imageWidth());
    serialized.add("image_height", options.imageHeight());
    serialized.add("image_mode", options.imageMode());
    serialized.add("image_anchor", options.imageAnchor());
    serialized.add("image_allow_upscale", options.imageAllowUpscale());
    serialized.add("image_background", options.imageBackground());
    serialized.add("image_quality", options.imageQuality());
    serialized.add("pdf_paper_format", options.pdfPaperFormat());
    serialized.add("pdf_landscape", options.pdfLandscape());
    serialized.add("pdf_print_background", options.pdfPrintBackground());
    serialized.add("pdf_margin", options.pdfMargin());
    serialized.add("pdf_margin_top", options.pdfMarginTop());
    serialized.add("pdf_margin_right", options.pdfMarginRight());
    serialized.add("pdf_margin_bottom", options.pdfMarginBottom());
    serialized.add("pdf_margin_left", options.pdfMarginLeft());
    serialized.add("pdf_scale", options.pdfScale());
    serialized.add("pdf_page_ranges", options.pdfPageRanges());
    serialized.add("cache", options.cache());
    serialized.add("cache_ttl", options.cacheTtl());
    serialized.add("cache_key", options.cacheKey());
    serialized.add("storage_mode", options.storageMode());
    serialized.add("storage_endpoint", options.storageEndpoint());
    serialized.add("storage_bucket", options.storageBucket());
    serialized.add("storage_region", options.storageRegion());
    serialized.add("storage_object_key", options.storageObjectKey());

    return serialized.build();
  }

  static String buildCanonicalQuery(List<WirePair> pairs, String accessKey) {
    List<WirePair> canonical = new ArrayList<>(pairs.size() + 1);
    canonical.addAll(pairs);
    canonical.add(new WirePair("access_key", accessKey));
    canonical.sort(Comparator.comparing(WirePair::name));
    return encodeQuery(canonical);
  }

  static String encodeQuery(List<WirePair> pairs) {
    StringBuilder query = new StringBuilder();
    for (WirePair pair : pairs) {
      if (!query.isEmpty()) {
        query.append('&');
      }
      query
          .append(URLEncoder.encode(pair.name(), StandardCharsets.UTF_8))
          .append('=')
          .append(URLEncoder.encode(pair.value(), StandardCharsets.UTF_8));
    }
    return query.toString();
  }

  static byte[] serializeJsonBody(Map<String, Object> body) {
    try {
      return JsonCodec.writeObject(body);
    } catch (Exception cause) {
      throw new ScreenshotScoutSerializationException(
          "The POST capture body could not be serialized as JSON.", null, cause);
    }
  }

  private static final class SerializationBuilder {
    private final List<WirePair> pairs = new ArrayList<>();
    private final LinkedHashMap<String, Object> body = new LinkedHashMap<>();

    private SerializationBuilder(String targetUrl) {
      pairs.add(new WirePair("url", targetUrl));
      body.put("url", targetUrl);
    }

    private void add(String wireName, Object value) {
      if (value == null) {
        return;
      }

      Object wireValue = value instanceof CaptureStringValue openValue ? openValue.value() : value;
      if (wireValue instanceof Double doubleValue && !Double.isFinite(doubleValue)) {
        throw new ScreenshotScoutSerializationException(
            "The capture option \"" + publicName(wireName) + "\" must be a finite number.",
            publicName(wireName));
      }
      if (!(wireValue instanceof String)
          && !(wireValue instanceof Number)
          && !(wireValue instanceof Boolean)) {
        throw new ScreenshotScoutSerializationException(
            "The capture option \"" + wireName + "\" must be a string, finite number, or boolean.",
            publicName(wireName));
      }

      if (wireValue instanceof Double doubleValue) {
        String formatted = EcmaScriptNumberFormatter.format(doubleValue);
        pairs.add(new WirePair(wireName, formatted));
        body.put(wireName, new WireNumber(formatted));
      } else {
        pairs.add(new WirePair(wireName, String.valueOf(wireValue)));
        body.put(wireName, wireValue);
      }
    }

    private void addRepeated(String publicName, String wireName, List<?> values) {
      if (values == null || values.isEmpty()) {
        return;
      }

      List<String> copied = new ArrayList<>(values.size());
      for (Object value : values) {
        if (!(value instanceof String stringValue)) {
          throw new ScreenshotScoutSerializationException(
              "The capture option \"" + publicName + "\" must contain only strings.", publicName);
        }
        copied.add(stringValue);
        pairs.add(new WirePair(wireName, stringValue));
      }
      body.put(wireName, Collections.unmodifiableList(copied));
    }

    private static String publicName(String wireName) {
      StringBuilder result = new StringBuilder(wireName.length());
      boolean uppercaseNext = false;
      for (int index = 0; index < wireName.length(); index++) {
        char character = wireName.charAt(index);
        if (character == '_') {
          uppercaseNext = true;
        } else if (uppercaseNext) {
          result.append(Character.toUpperCase(character));
          uppercaseNext = false;
        } else {
          result.append(character);
        }
      }
      return result.toString();
    }

    private SerializedCaptureOptions build() {
      return new SerializedCaptureOptions(
          Collections.unmodifiableList(new ArrayList<>(pairs)),
          Collections.unmodifiableMap(new LinkedHashMap<>(body)));
    }
  }
}
