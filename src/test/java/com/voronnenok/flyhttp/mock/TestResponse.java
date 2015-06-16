package com.voronnenok.flyhttp.mock;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;

/**
 * Created by voronnenok on 15.06.15.
 */
public class TestResponse extends BasicHttpResponse {
    private final HttpEntity httpEntity;
    private final Header[] headers;
    private StatusLine statusLine;

    public TestResponse(int statusCode, HttpEntity httpEntity, Header[] headers) {
        super(new ProtocolVersion("HTTP", 1, 1), statusCode, "OK");
        this.statusLine = new TestStatusLine(statusCode);
        this.httpEntity = httpEntity;
        this.headers = headers;
    }

    @Override
    public Header[] getAllHeaders() {
        return headers;
    }

    @Override
    public HttpEntity getEntity() {
        return httpEntity;
    }

    @Override
    public StatusLine getStatusLine() {
        return statusLine;
    }
}
