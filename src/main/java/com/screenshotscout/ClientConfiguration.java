package com.screenshotscout;

import java.net.http.HttpClient;

record ClientConfiguration(String accessKey, String secretKey, HttpClient httpClient) {}
