package com.rbkmoney.hooker.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PostSender {

    private final OkHttpClient httpClient;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String SIGNATURE_HEADER = "Content-Signature";
    public static final long RESPONSE_MAX_LENGTH = 4096L;

    public PostSender(int connectionPoolSize, int timeout) {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        ConnectionPool connectionPool = new ConnectionPool(2 * connectionPoolSize, 5, TimeUnit.MINUTES);
        this.httpClient = httpBuilder
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(false)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public int doPost(String url, long messageId, String paramsAsString, String signature) throws IOException {
        log.info("Sending message with id {}, {} to hook: {} ", messageId, paramsAsString, url);

        RequestBody body = RequestBody.create(JSON, paramsAsString);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader(SIGNATURE_HEADER, "alg=RS256; digest=" + signature)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            log.info("Response from hook: messageId: {}, code: {}; body: {}", messageId, response.code(), response.body() != null ? response.peekBody(RESPONSE_MAX_LENGTH).string() : "<empty>");
            return response.code();
        }
    }
}
