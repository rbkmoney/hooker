package com.rbkmoney.hooker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
public class PostSender {

    private int timeout;
    private HttpClient httpClient;
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String SIGNATURE_HEADER = "Content-Signature";
    public static final int RESPONSE_MAX_LENGTH = 4096;

    public PostSender(@Value("${merchant.callback.timeout}") int timeout) {
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
    }

    public int doPost(String url, long messageId, String paramsAsString, String signature) throws Exception {
        log.info("Sending message with id {}, {} to hook: {} ", messageId, paramsAsString, url);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .timeout(Duration.ofSeconds(timeout))
                .header(CONTENT_TYPE_HEADER, "application/json; charset=utf-8")
                .header(SIGNATURE_HEADER, "alg=RS256; digest=" + signature)
                .POST(HttpRequest.BodyPublishers.ofString(paramsAsString))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        String resultBody = body != null ? body.substring(0, Math.min(body.length(), RESPONSE_MAX_LENGTH)) : "<empty>";
        log.info("Response from hook: messageId: {}, code: {}; body: {}", messageId, response.statusCode(), resultBody);
        return response.statusCode();
    }
}
