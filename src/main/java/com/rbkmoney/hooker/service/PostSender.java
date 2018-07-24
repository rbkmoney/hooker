package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.logging.HttpLoggingInterceptor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class PostSender {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private final OkHttpClient httpClient;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String SIGNATURE_HEADER = "Content-Signature";

    public PostSender(@Value("${merchant.callback.timeout}") int timeout) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
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

        Response response = httpClient.newCall(request).execute();
        log.info("Response from hook: messageId: {}, code: {}; body: {}", messageId, response.code(), response.body().string());
        return response.code();
    }
}
