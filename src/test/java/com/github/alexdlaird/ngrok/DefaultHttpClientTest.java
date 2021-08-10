/*
 * Copyright (c) 2021 Alex Laird
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.alexdlaird.ngrok;

import com.github.alexdlaird.http.DefaultHttpClient;
import com.github.alexdlaird.http.Parameter;
import com.github.alexdlaird.http.Response;
import com.github.alexdlaird.ngrok.http.CaptureRequestResponse;
import com.github.alexdlaird.ngrok.http.TunnelRequest;
import com.github.alexdlaird.ngrok.http.TunnelResponse;
import com.github.alexdlaird.ngrok.http.TunnelsResponse;
import com.github.alexdlaird.ngrok.process.NgrokProcess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultHttpClientTest {

    private final DefaultHttpClient defaultHttpClient = new DefaultHttpClient.Builder("http://localhost:4040").build();

    private final NgrokProcess ngrokProcess = new NgrokProcess();

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        ngrokProcess.connect();
        Thread.sleep(5000);
    }

    @AfterEach
    public void tearDown() {
        ngrokProcess.disconnect();
    }

    @Test
    public void testPost() {
        // GIVEN
        final TunnelRequest request = new TunnelRequest.Builder("my-tunnel", "http", "80").build();

        // WHEN
        final Response<TunnelResponse> createResponse = defaultHttpClient.post("/api/tunnels", request, Collections.emptyList(), Collections.emptyMap(), TunnelResponse.class);

        // THEN
        assertEquals(createResponse.getStatusCode(), 201);
        assertEquals(createResponse.getBody().getName(), "my-tunnel");
        assertEquals(createResponse.getBody().getProto(), "http");
        assertEquals(createResponse.getBody().getConfig().getAddr(), "http://localhost:80");
    }

    @Test
    public void testGet() {
        // GIVEN
        final TunnelRequest request = new TunnelRequest.Builder("my-tunnel", "http", "80").build();
        defaultHttpClient.post("/api/tunnels", request, Collections.emptyList(), Collections.emptyMap(), TunnelResponse.class);

        // WHEN
        final Response<TunnelsResponse> getResponse = defaultHttpClient.get("/api/tunnels", Collections.emptyList(), Collections.emptyMap(), TunnelsResponse.class);

        // THEN
        assertEquals(getResponse.getStatusCode(), 200);
        assertEquals(getResponse.getBody().getTunnels().size(), 1);
        assertEquals(getResponse.getBody().getTunnels().get(0).getName(), "my-tunnel");
        assertEquals(getResponse.getBody().getTunnels().get(0).getProto(), "http");
        assertEquals(getResponse.getBody().getTunnels().get(0).getConfig().getAddr(), "http://localhost:80");
    }

    @Test
    public void testDelete() {
        // GIVEN
        final TunnelRequest request = new TunnelRequest.Builder("my-tunnel", "http", "80").build();
        final Response<TunnelResponse> createResponse = defaultHttpClient.post("/api/tunnels", request, Collections.emptyList(), Collections.emptyMap(), TunnelResponse.class);

        // WHEN
        final Response<?> deleteResponse = defaultHttpClient.delete(createResponse.getBody().getUri(), Collections.emptyList(), Collections.emptyMap());

        // THEN
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @Test
    public void testGetWithQueryParameters() throws InterruptedException {
        // GIVEN
        final TunnelRequest request = new TunnelRequest.Builder("tunnel (1)", "http", "4040").withBindTls().build();
        final Response<TunnelResponse> createResponse = defaultHttpClient.post("/api/tunnels", request, Collections.emptyList(), Collections.emptyMap(), TunnelResponse.class);
        final String publicUrl = createResponse.getBody().getPublicUrl();
        final DefaultHttpClient publicHttpClient = new DefaultHttpClient.Builder(publicUrl).build();

        Thread.sleep(1000);

        publicHttpClient.get("/status", Collections.emptyList(), Collections.emptyMap(), Object.class);

        Thread.sleep(3000);

        // WHEN
        final Response<CaptureRequestResponse> response1 = defaultHttpClient.get("/api/requests/http", Collections.emptyList(), Collections.emptyMap(), CaptureRequestResponse.class);
        final Response<CaptureRequestResponse> response2 = defaultHttpClient.get("/api/requests/http", List.of(new Parameter("tunnel_name", "tunnel (1)")), Collections.emptyMap(), CaptureRequestResponse.class);
        final Response<CaptureRequestResponse> response3 = defaultHttpClient.get("/api/requests/http", List.of(new Parameter("tunnel_name", "tunnel (1) (http)")), Collections.emptyMap(), CaptureRequestResponse.class);

        // THEN
        assertEquals(response1.getStatusCode(), 200);
        assertTrue(response1.getBody().getRequests().size() > 0);
        assertEquals(response2.getStatusCode(), 200);
        assertTrue(response2.getBody().getRequests().size() > 0);
        assertEquals(response3.getStatusCode(), 200);
        assertEquals(response3.getBody().getRequests().size(), 0);
    }
}
