package com.voronnenok.flyhttp;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by voronnenok on 01.06.15.
 */
public interface HttpClient {
    public HttpResponse fireRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException;
}
