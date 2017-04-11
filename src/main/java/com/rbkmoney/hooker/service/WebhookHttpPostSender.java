package com.rbkmoney.hooker.service;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class WebhookHttpPostSender {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private final OkHttpClient httpClient;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String SIGNATURE_HEADER = "X-Signature";

    public WebhookHttpPostSender(@Value("${merchant.callback.timeout}") int timeout) {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public int doPost(String url, String paramsAsString, String signature) throws IOException {
        log.info("WebhookHttpPostSender.doPost start sending webhook to merchant. Url " + url);
        log.debug("Body: "+paramsAsString);
        RequestBody body = RequestBody.create(JSON, paramsAsString);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader(SIGNATURE_HEADER, signature)
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        log.info("WebhookHttpPostSender.doPost webhook send with code "+response.code());
        return response.code();
    }
}
