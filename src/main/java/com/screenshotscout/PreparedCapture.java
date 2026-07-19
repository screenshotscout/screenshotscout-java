package com.screenshotscout;

import java.net.http.HttpRequest;

record PreparedCapture(HttpRequest request, ExpectedResponseKind expectedKind) {}
