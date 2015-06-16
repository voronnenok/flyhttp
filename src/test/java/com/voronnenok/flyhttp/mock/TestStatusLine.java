package com.voronnenok.flyhttp.mock;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

/**
 * Created by voronnenok on 16.06.15.
 */
public class TestStatusLine implements StatusLine {

    private final int statusCode;
    private final ProtocolVersion protocolVersion;
    private final String reasonPhrase;

    public TestStatusLine(int statusCode) {
        this.statusCode = statusCode;
        this.protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        this.reasonPhrase = "OK";
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}
