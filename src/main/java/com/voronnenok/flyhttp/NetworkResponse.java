package com.voronnenok.flyhttp;

import java.util.Map;

/**
 * Created by voronnenok on 26.05.15.
 */
public class NetworkResponse {
    public boolean notModified;
    public byte[] data;
    public int statusCode;
    public Map<String, String> headers;

    public NetworkResponse(boolean notModified, byte[] data, int statusCode, Map<String, String> headers) {
        this.notModified = notModified;
        this.data = data;
        this.statusCode = statusCode;
        this.headers = headers;
    }

}
