package com.rbkmoney.hooker;

/**
 * Created by inalarsanukaev on 11.05.17.
 */

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * @since 12.04.17
 **/
@Slf4j
public class MockServerTest {

    private static final int PORT = 8089;
    private static final String body = "xyi";

    @Test
    public void testMockServer() throws Exception {

        final Dispatcher dispatcher = new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    request.getBody().writeTo(byteArrayOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                log.info("\nRequest: " + request.getRequestLine() + "\nBody: " + byteArrayOutputStream.toString());
                return new MockResponse().setBody(body).setResponseCode(200);
            }
        };
        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        MockWebServer server = new MockWebServer();
        server.setDispatcher(dispatcher);

        // Start the server.

        server.start(PORT);
        log.info("Server started on port: " + server.getPort());
        log.info("To run it : \n ngrok http " + PORT);

        // process request
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    server.takeRequest();
                } catch (InterruptedException e) {
                    try {
                        server.shutdown();
                    } catch (IOException e1) {
                        new RuntimeException(e1);
                    }
                }
            }
        });

        thread.start();

        OkHttpClient client = new OkHttpClient.Builder().build();

        Response response = client.newCall(new Request.Builder()
                .url("http://" + server.getHostName() + ":" + server.getPort())
                .get()
                .build()
        ).execute();

        Assert.assertEquals(body, response.body().string());

        thread.interrupt();
    }
}
