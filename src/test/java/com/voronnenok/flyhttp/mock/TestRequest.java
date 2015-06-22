package com.voronnenok.flyhttp.mock;

import com.voronnenok.flyhttp.NetworkResponse;
import com.voronnenok.flyhttp.Request;
import com.voronnenok.flyhttp.Response;

/**
 * Created by voronnenok on 09.06.15.
 */
public class TestRequest extends Request<Object> {
    private final static String TEST_URL = "http://vk.com";
    final byte[] data;

    public TestRequest() {
        super(TEST_URL, null);
        this.data = null;
    }

    public TestRequest(byte[] data) {
        super(TEST_URL, null);
        this.data = data;
    }

    @Override
    protected Response<Object> parseNetworkResponse(NetworkResponse networkResponse) {
        return null;
    }

    @Override
    protected byte[] getBody() {
        return data;
    }
}
