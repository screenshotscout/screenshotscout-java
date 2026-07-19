package com.screenshotscout;

import java.util.List;
import java.util.Map;

record SerializedCaptureOptions(List<WirePair> pairs, Map<String, Object> body) {}
