package com.voronnenok.flyhttp.mock;

import com.voronnenok.flyhttp.NetworkResponse;

import java.util.Map;

/**
 * Created by voronnenok on 22.06.15.
 */
public class TestNetworkResponse extends NetworkResponse {
    public TestNetworkResponse(boolean notModified, byte[] data, int statusCode, Map<String, String> headers) {
        super(notModified, data, statusCode, headers);
    }
}
