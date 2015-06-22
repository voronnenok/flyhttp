package com.voronnenok.flyhttp;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

import com.voronnenok.flyhttp.errors.FlyError;

/**
 * Created by voronnenok on 26.05.15.
 *
 * Request to send. It's a container that holds all http-request data.
 * Used with abstract data <Result> that represents the data that we need to return to listener of request.
 */
abstract public class Request<Result> {
    protected final String DEFAULT_CHARSET = "utf-8";
    protected final String DEFAULT_CONTENT_TYPE = "application/json";
    protected final int DEFAULT_TIMEOUT = 10000;
    private final String url;
    private final Method method;
    private final Map<String, String> headers = Collections.emptyMap();
    private final Listener<Result> listener;
    private final String body;
    private volatile boolean isCanceled = false;

    public Request(String url, Listener<Result> listener) {
        this(url, Method.GET, null, listener);
    }

    public Request(String url, String body, Listener<Result> listener) {
        this(url, Method.POST, body, listener);
    }

    public Request(String url, Method method, String body, Listener<Result> listener) {
        this.url = url;
        this.method = method;
        this.body = body;
        this.listener = listener;
    }

    String getUrl() {
        return url;
    }

    Method getMethod() {
        return method;
    }

    Map<String, String> getHeaders() {
        return headers;
    }

    Map<String, String> getParams() {
        return null;
    }

    public int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    public String getBodyContentType() {
        return DEFAULT_CONTENT_TYPE;
    }

    protected byte[] getBody() {
        try {
            return body == null ? null : body.getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract Response<Result> parseNetworkResponse(NetworkResponse networkResponse);

    void deliverResponse(Response<Result> response) {
        if(listener != null) {
            if(response.isSuccess()) {
                listener.onSuccess(response.data);
            } else {
                listener.onError(response.error);
            }
        }
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void cancel() {
        this.isCanceled = true;
    }

    public static enum Method {
        GET,
        POST,
    }

    public static interface Listener<Result> {
        public void onSuccess(Result response);
        public void onError(FlyError error);
    }

}
