package com.voronnenok.flyhttp.mock;

import com.voronnenok.flyhttp.HttpClient;
import com.voronnenok.flyhttp.Request;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by voronnenok on 15.06.15.
 */
public class TestClient implements HttpClient {
    @Override
    public HttpResponse fireRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException {

        return null;
    }
}
