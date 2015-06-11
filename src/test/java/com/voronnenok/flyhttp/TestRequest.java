package com.voronnenok.flyhttp;

/**
 * Created by voronnenok on 09.06.15.
 */
public class TestRequest extends Request<Object> {
    private final static String TEST_URL = "http://vk.com";
    public TestRequest() {
        super(TEST_URL, null);
    }

    @Override
    Response<Object> parseNetworkResponse(NetworkResponse networkResponse) {
        return null;
    }
}
