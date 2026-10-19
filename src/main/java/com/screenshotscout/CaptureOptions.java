package com.screenshotscout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable Screenshot Scout capture options.
 *
 * <p>The target URL, credentials, signature, and HTTP method are supplied separately. Null values
 * are omitted. Repeated values preserve caller order and duplicates.
 */
public final class CaptureOptions {
  private final CaptureFormat format;
  private final CaptureResponseType responseType;
  private final String country;
  private final String proxy;
  private final Double geolocationLatitude;
  private final Double geolocationLongitude;
  private final Double geolocationAccuracy;
  private final List<String> cookies;
  private final List<String> headers;
  private final Integer timeout;
  private final CaptureWaitUntil waitUntil;
  private final Integer navigationTimeout;
  private final Integer delay;
  private final String device;
  private final Integer deviceViewportWidth;
  private final Integer deviceViewportHeight;
  private final Double deviceScaleFactor;
  private final Boolean deviceIsMobile;
  private final Boolean deviceHasTouch;
  private final String deviceUserAgent;
  private final String timezone;
  private final CaptureMediaType mediaType;
  private final CaptureColorScheme colorScheme;
  private final Boolean reducedMotion;
  private final Boolean fullPage;
  private final Boolean fullPagePreScroll;
  private final Integer fullPagePreScrollStep;
  private final Integer fullPagePreScrollStepDelay;
  private final Integer fullPageMaxHeight;
  private final Boolean blockCookieBanners;
  private final Boolean blockAds;
  private final Boolean blockChatWidgets;
  private final List<String> hideSelectors;
  private final List<String> clickSelectors;
  private final List<String> clickAllSelectors;
  private final List<String> injectCss;
  private final List<String> injectJs;
  private final Boolean bypassCsp;
  private final String selector;
  private final Integer clipX;
  private final Integer clipY;
  private final Integer clipWidth;
  private final Integer clipHeight;
  private final Integer imageWidth;
  private final Integer imageHeight;
  private final CaptureImageMode imageMode;
  private final CaptureImageAnchor imageAnchor;
  private final Boolean imageAllowUpscale;
  private final String imageBackground;
  private final Integer imageQuality;
  private final CapturePdfPaperFormat pdfPaperFormat;
  private final Boolean pdfLandscape;
  private final Boolean pdfPrintBackground;
  private final String pdfMargin;
  private final String pdfMarginTop;
  private final String pdfMarginRight;
  private final String pdfMarginBottom;
  private final String pdfMarginLeft;
  private final Double pdfScale;
  private final String pdfPageRanges;
  private final Boolean cache;
  private final Integer cacheTtl;
  private final String cacheKey;
  private final CaptureStorageMode storageMode;
  private final String storageEndpoint;
  private final String storageBucket;
  private final String storageRegion;
  private final String storageObjectKey;

  private CaptureOptions(Builder builder) {
    format = builder.format;
    responseType = builder.responseType;
    country = builder.country;
    proxy = builder.proxy;
    geolocationLatitude = builder.geolocationLatitude;
    geolocationLongitude = builder.geolocationLongitude;
    geolocationAccuracy = builder.geolocationAccuracy;
    cookies = immutableList(builder.cookies);
    headers = immutableList(builder.headers);
    timeout = builder.timeout;
    waitUntil = builder.waitUntil;
    navigationTimeout = builder.navigationTimeout;
    delay = builder.delay;
    device = builder.device;
    deviceViewportWidth = builder.deviceViewportWidth;
    deviceViewportHeight = builder.deviceViewportHeight;
    deviceScaleFactor = builder.deviceScaleFactor;
    deviceIsMobile = builder.deviceIsMobile;
    deviceHasTouch = builder.deviceHasTouch;
    deviceUserAgent = builder.deviceUserAgent;
    timezone = builder.timezone;
    mediaType = builder.mediaType;
    colorScheme = builder.colorScheme;
    reducedMotion = builder.reducedMotion;
    fullPage = builder.fullPage;
    fullPagePreScroll = builder.fullPagePreScroll;
    fullPagePreScrollStep = builder.fullPagePreScrollStep;
    fullPagePreScrollStepDelay = builder.fullPagePreScrollStepDelay;
    fullPageMaxHeight = builder.fullPageMaxHeight;
    blockCookieBanners = builder.blockCookieBanners;
    blockAds = builder.blockAds;
    blockChatWidgets = builder.blockChatWidgets;
    hideSelectors = immutableList(builder.hideSelectors);
    clickSelectors = immutableList(builder.clickSelectors);
    clickAllSelectors = immutableList(builder.clickAllSelectors);
    injectCss = immutableList(builder.injectCss);
    injectJs = immutableList(builder.injectJs);
    bypassCsp = builder.bypassCsp;
    selector = builder.selector;
    clipX = builder.clipX;
    clipY = builder.clipY;
    clipWidth = builder.clipWidth;
    clipHeight = builder.clipHeight;
    imageWidth = builder.imageWidth;
    imageHeight = builder.imageHeight;
    imageMode = builder.imageMode;
    imageAnchor = builder.imageAnchor;
    imageAllowUpscale = builder.imageAllowUpscale;
    imageBackground = builder.imageBackground;
    imageQuality = builder.imageQuality;
    pdfPaperFormat = builder.pdfPaperFormat;
    pdfLandscape = builder.pdfLandscape;
    pdfPrintBackground = builder.pdfPrintBackground;
    pdfMargin = builder.pdfMargin;
    pdfMarginTop = builder.pdfMarginTop;
    pdfMarginRight = builder.pdfMarginRight;
    pdfMarginBottom = builder.pdfMarginBottom;
    pdfMarginLeft = builder.pdfMarginLeft;
    pdfScale = builder.pdfScale;
    pdfPageRanges = builder.pdfPageRanges;
    cache = builder.cache;
    cacheTtl = builder.cacheTtl;
    cacheKey = builder.cacheKey;
    storageMode = builder.storageMode;
    storageEndpoint = builder.storageEndpoint;
    storageBucket = builder.storageBucket;
    storageRegion = builder.storageRegion;
    storageObjectKey = builder.storageObjectKey;
  }

  public static Builder builder() {
    return new Builder();
  }

  public CaptureFormat format() {
    return format;
  }

  public CaptureResponseType responseType() {
    return responseType;
  }

  public String country() {
    return country;
  }

  public String proxy() {
    return proxy;
  }

  public Double geolocationLatitude() {
    return geolocationLatitude;
  }

  public Double geolocationLongitude() {
    return geolocationLongitude;
  }

  public Double geolocationAccuracy() {
    return geolocationAccuracy;
  }

  public List<String> cookies() {
    return cookies;
  }

  public List<String> headers() {
    return headers;
  }

  public Integer timeout() {
    return timeout;
  }

  public CaptureWaitUntil waitUntil() {
    return waitUntil;
  }

  public Integer navigationTimeout() {
    return navigationTimeout;
  }

  public Integer delay() {
    return delay;
  }

  public String device() {
    return device;
  }

  public Integer deviceViewportWidth() {
    return deviceViewportWidth;
  }

  public Integer deviceViewportHeight() {
    return deviceViewportHeight;
  }

  public Double deviceScaleFactor() {
    return deviceScaleFactor;
  }

  public Boolean deviceIsMobile() {
    return deviceIsMobile;
  }

  public Boolean deviceHasTouch() {
    return deviceHasTouch;
  }

  public String deviceUserAgent() {
    return deviceUserAgent;
  }

  public String timezone() {
    return timezone;
  }

  public CaptureMediaType mediaType() {
    return mediaType;
  }

  public CaptureColorScheme colorScheme() {
    return colorScheme;
  }

  public Boolean reducedMotion() {
    return reducedMotion;
  }

  public Boolean fullPage() {
    return fullPage;
  }

  public Boolean fullPagePreScroll() {
    return fullPagePreScroll;
  }

  public Integer fullPagePreScrollStep() {
    return fullPagePreScrollStep;
  }

  public Integer fullPagePreScrollStepDelay() {
    return fullPagePreScrollStepDelay;
  }

  public Integer fullPageMaxHeight() {
    return fullPageMaxHeight;
  }

  public Boolean blockCookieBanners() {
    return blockCookieBanners;
  }

  public Boolean blockAds() {
    return blockAds;
  }

  public Boolean blockChatWidgets() {
    return blockChatWidgets;
  }

  public List<String> hideSelectors() {
    return hideSelectors;
  }

  public List<String> clickSelectors() {
    return clickSelectors;
  }

  public List<String> clickAllSelectors() {
    return clickAllSelectors;
  }

  public List<String> injectCss() {
    return injectCss;
  }

  public List<String> injectJs() {
    return injectJs;
  }

  public Boolean bypassCsp() {
    return bypassCsp;
  }

  public String selector() {
    return selector;
  }

  public Integer clipX() {
    return clipX;
  }

  public Integer clipY() {
    return clipY;
  }

  public Integer clipWidth() {
    return clipWidth;
  }

  public Integer clipHeight() {
    return clipHeight;
  }

  public Integer imageWidth() {
    return imageWidth;
  }

  public Integer imageHeight() {
    return imageHeight;
  }

  public CaptureImageMode imageMode() {
    return imageMode;
  }

  public CaptureImageAnchor imageAnchor() {
    return imageAnchor;
  }

  public Boolean imageAllowUpscale() {
    return imageAllowUpscale;
  }

  public String imageBackground() {
    return imageBackground;
  }

  public Integer imageQuality() {
    return imageQuality;
  }

  public CapturePdfPaperFormat pdfPaperFormat() {
    return pdfPaperFormat;
  }

  public Boolean pdfLandscape() {
    return pdfLandscape;
  }

  public Boolean pdfPrintBackground() {
    return pdfPrintBackground;
  }

  public String pdfMargin() {
    return pdfMargin;
  }

  public String pdfMarginTop() {
    return pdfMarginTop;
  }

  public String pdfMarginRight() {
    return pdfMarginRight;
  }

  public String pdfMarginBottom() {
    return pdfMarginBottom;
  }

  public String pdfMarginLeft() {
    return pdfMarginLeft;
  }

  public Double pdfScale() {
    return pdfScale;
  }

  public String pdfPageRanges() {
    return pdfPageRanges;
  }

  public Boolean cache() {
    return cache;
  }

  public Integer cacheTtl() {
    return cacheTtl;
  }

  public String cacheKey() {
    return cacheKey;
  }

  public CaptureStorageMode storageMode() {
    return storageMode;
  }

  public String storageEndpoint() {
    return storageEndpoint;
  }

  public String storageBucket() {
    return storageBucket;
  }

  public String storageRegion() {
    return storageRegion;
  }

  public String storageObjectKey() {
    return storageObjectKey;
  }

  private static List<String> immutableList(List<String> values) {
    return values == null ? null : Collections.unmodifiableList(new ArrayList<>(values));
  }

  /** Mutable builder used to create an immutable {@link CaptureOptions} instance. */
  public static final class Builder {
    private CaptureFormat format;
    private CaptureResponseType responseType;
    private String country;
    private String proxy;
    private Double geolocationLatitude;
    private Double geolocationLongitude;
    private Double geolocationAccuracy;
    private List<String> cookies;
    private List<String> headers;
    private Integer timeout;
    private CaptureWaitUntil waitUntil;
    private Integer navigationTimeout;
    private Integer delay;
    private String device;
    private Integer deviceViewportWidth;
    private Integer deviceViewportHeight;
    private Double deviceScaleFactor;
    private Boolean deviceIsMobile;
    private Boolean deviceHasTouch;
    private String deviceUserAgent;
    private String timezone;
    private CaptureMediaType mediaType;
    private CaptureColorScheme colorScheme;
    private Boolean reducedMotion;
    private Boolean fullPage;
    private Boolean fullPagePreScroll;
    private Integer fullPagePreScrollStep;
    private Integer fullPagePreScrollStepDelay;
    private Integer fullPageMaxHeight;
    private Boolean blockCookieBanners;
    private Boolean blockAds;
    private Boolean blockChatWidgets;
    private List<String> hideSelectors;
    private List<String> clickSelectors;
    private List<String> clickAllSelectors;
    private List<String> injectCss;
    private List<String> injectJs;
    private Boolean bypassCsp;
    private String selector;
    private Integer clipX;
    private Integer clipY;
    private Integer clipWidth;
    private Integer clipHeight;
    private Integer imageWidth;
    private Integer imageHeight;
    private CaptureImageMode imageMode;
    private CaptureImageAnchor imageAnchor;
    private Boolean imageAllowUpscale;
    private String imageBackground;
    private Integer imageQuality;
    private CapturePdfPaperFormat pdfPaperFormat;
    private Boolean pdfLandscape;
    private Boolean pdfPrintBackground;
    private String pdfMargin;
    private String pdfMarginTop;
    private String pdfMarginRight;
    private String pdfMarginBottom;
    private String pdfMarginLeft;
    private Double pdfScale;
    private String pdfPageRanges;
    private Boolean cache;
    private Integer cacheTtl;
    private String cacheKey;
    private CaptureStorageMode storageMode;
    private String storageEndpoint;
    private String storageBucket;
    private String storageRegion;
    private String storageObjectKey;

    private Builder() {}

    public Builder format(CaptureFormat value) {
      format = value;
      return this;
    }

    public Builder responseType(CaptureResponseType value) {
      responseType = value;
      return this;
    }

    public Builder country(String value) {
      country = value;
      return this;
    }

    public Builder proxy(String value) {
      proxy = value;
      return this;
    }

    public Builder geolocationLatitude(Double value) {
      geolocationLatitude = value;
      return this;
    }

    public Builder geolocationLongitude(Double value) {
      geolocationLongitude = value;
      return this;
    }

    public Builder geolocationAccuracy(Double value) {
      geolocationAccuracy = value;
      return this;
    }

    public Builder cookies(List<String> value) {
      cookies = value;
      return this;
    }

    public Builder headers(List<String> value) {
      headers = value;
      return this;
    }

    public Builder timeout(Integer value) {
      timeout = value;
      return this;
    }

    public Builder waitUntil(CaptureWaitUntil value) {
      waitUntil = value;
      return this;
    }

    public Builder navigationTimeout(Integer value) {
      navigationTimeout = value;
      return this;
    }

    public Builder delay(Integer value) {
      delay = value;
      return this;
    }

    public Builder device(String value) {
      device = value;
      return this;
    }

    public Builder deviceViewportWidth(Integer value) {
      deviceViewportWidth = value;
      return this;
    }

    public Builder deviceViewportHeight(Integer value) {
      deviceViewportHeight = value;
      return this;
    }

    public Builder deviceScaleFactor(Double value) {
      deviceScaleFactor = value;
      return this;
    }

    public Builder deviceIsMobile(Boolean value) {
      deviceIsMobile = value;
      return this;
    }

    public Builder deviceHasTouch(Boolean value) {
      deviceHasTouch = value;
      return this;
    }

    public Builder deviceUserAgent(String value) {
      deviceUserAgent = value;
      return this;
    }

    public Builder timezone(String value) {
      timezone = value;
      return this;
    }

    public Builder mediaType(CaptureMediaType value) {
      mediaType = value;
      return this;
    }

    public Builder colorScheme(CaptureColorScheme value) {
      colorScheme = value;
      return this;
    }

    public Builder reducedMotion(Boolean value) {
      reducedMotion = value;
      return this;
    }

    public Builder fullPage(Boolean value) {
      fullPage = value;
      return this;
    }

    public Builder fullPagePreScroll(Boolean value) {
      fullPagePreScroll = value;
      return this;
    }

    public Builder fullPagePreScrollStep(Integer value) {
      fullPagePreScrollStep = value;
      return this;
    }

    public Builder fullPagePreScrollStepDelay(Integer value) {
      fullPagePreScrollStepDelay = value;
      return this;
    }

    public Builder fullPageMaxHeight(Integer value) {
      fullPageMaxHeight = value;
      return this;
    }

    public Builder blockCookieBanners(Boolean value) {
      blockCookieBanners = value;
      return this;
    }

    public Builder blockAds(Boolean value) {
      blockAds = value;
      return this;
    }

    public Builder blockChatWidgets(Boolean value) {
      blockChatWidgets = value;
      return this;
    }

    public Builder hideSelectors(List<String> value) {
      hideSelectors = value;
      return this;
    }

    public Builder clickSelectors(List<String> value) {
      clickSelectors = value;
      return this;
    }

    public Builder clickAllSelectors(List<String> value) {
      clickAllSelectors = value;
      return this;
    }

    public Builder injectCss(List<String> value) {
      injectCss = value;
      return this;
    }

    public Builder injectJs(List<String> value) {
      injectJs = value;
      return this;
    }

    public Builder bypassCsp(Boolean value) {
      bypassCsp = value;
      return this;
    }

    public Builder selector(String value) {
      selector = value;
      return this;
    }

    public Builder clipX(Integer value) {
      clipX = value;
      return this;
    }

    public Builder clipY(Integer value) {
      clipY = value;
      return this;
    }

    public Builder clipWidth(Integer value) {
      clipWidth = value;
      return this;
    }

    public Builder clipHeight(Integer value) {
      clipHeight = value;
      return this;
    }

    public Builder imageWidth(Integer value) {
      imageWidth = value;
      return this;
    }

    public Builder imageHeight(Integer value) {
      imageHeight = value;
      return this;
    }

    public Builder imageMode(CaptureImageMode value) {
      imageMode = value;
      return this;
    }

    public Builder imageAnchor(CaptureImageAnchor value) {
      imageAnchor = value;
      return this;
    }

    public Builder imageAllowUpscale(Boolean value) {
      imageAllowUpscale = value;
      return this;
    }

    public Builder imageBackground(String value) {
      imageBackground = value;
      return this;
    }

    public Builder imageQuality(Integer value) {
      imageQuality = value;
      return this;
    }

    public Builder pdfPaperFormat(CapturePdfPaperFormat value) {
      pdfPaperFormat = value;
      return this;
    }

    public Builder pdfLandscape(Boolean value) {
      pdfLandscape = value;
      return this;
    }

    public Builder pdfPrintBackground(Boolean value) {
      pdfPrintBackground = value;
      return this;
    }

    public Builder pdfMargin(String value) {
      pdfMargin = value;
      return this;
    }

    public Builder pdfMarginTop(String value) {
      pdfMarginTop = value;
      return this;
    }

    public Builder pdfMarginRight(String value) {
      pdfMarginRight = value;
      return this;
    }

    public Builder pdfMarginBottom(String value) {
      pdfMarginBottom = value;
      return this;
    }

    public Builder pdfMarginLeft(String value) {
      pdfMarginLeft = value;
      return this;
    }

    public Builder pdfScale(Double value) {
      pdfScale = value;
      return this;
    }

    public Builder pdfPageRanges(String value) {
      pdfPageRanges = value;
      return this;
    }

    public Builder cache(Boolean value) {
      cache = value;
      return this;
    }

    public Builder cacheTtl(Integer value) {
      cacheTtl = value;
      return this;
    }

    public Builder cacheKey(String value) {
      cacheKey = value;
      return this;
    }

    public Builder storageMode(CaptureStorageMode value) {
      storageMode = value;
      return this;
    }

    public Builder storageEndpoint(String value) {
      storageEndpoint = value;
      return this;
    }

    public Builder storageBucket(String value) {
      storageBucket = value;
      return this;
    }

    public Builder storageRegion(String value) {
      storageRegion = value;
      return this;
    }

    public Builder storageObjectKey(String value) {
      storageObjectKey = value;
      return this;
    }

    public CaptureOptions build() {
      return new CaptureOptions(this);
    }
  }
}
