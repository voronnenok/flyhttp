package com.voronnenok.flyhttp;

import java.nio.charset.Charset;

import com.voronnenok.flyhttp.errors.FlyError;

/**
 * Created by voronnenok on 03.06.15.
 */
public class StringRequest extends Request<String> {
    public StringRequest(String url, Listener<String> listener) {
        super(url, listener);
    }

    public StringRequest(String url, String body, Listener<String> listener) {
        super(url, body, listener);
    }

    public StringRequest(String url, Method method, String body, Listener<String> listener) {
        super(url, method, body, listener);
    }

    @Override
    Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
        String str = new String(networkResponse.data, Charset.forName(DEFAULT_CHARSET));
        if(networkResponse.statusCode < 400) {
            return Response.success(str);
        }
        return Response.error(new FlyError(str));
    }
}
