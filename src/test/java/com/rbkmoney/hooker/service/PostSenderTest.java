package com.rbkmoney.hooker.service;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class PostSenderTest {

    @Test
    public void doPost() throws Exception {
        PostSender postSender = new PostSender(1);
        MockWebServer server = new MockWebServer();
        HttpUrl httpUrl = server.url("/");

        // happy case
        server.enqueue(new MockResponse().setResponseCode(200).setBody("kek"));
        int statusCode = postSender.doPost(httpUrl.toString(), 1, "kek", "kek");
        assertEquals(200, statusCode);

        // bad status code
        server.enqueue(new MockResponse().setResponseCode(403).setBody("wrong"));
        statusCode = postSender.doPost(httpUrl.toString(), 1, "kek", "kek");
        assertEquals(403, statusCode);

        // empty body
        server.enqueue(new MockResponse().setResponseCode(301));
        statusCode = postSender.doPost(httpUrl.toString(), 1, "kek", "kek");
        assertEquals(301, statusCode);

        // connection timeout
        server.enqueue(new MockResponse().setResponseCode(501).setBody("kek").setBodyDelay(3, TimeUnit.SECONDS));
        statusCode = postSender.doPost(httpUrl.toString(), 1, "kek", "kek");
        assertEquals(501, statusCode);
    }
}
