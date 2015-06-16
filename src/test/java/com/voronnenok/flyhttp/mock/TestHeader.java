package com.voronnenok.flyhttp.mock;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;

/**
 * Created by voronnenok on 16.06.15.
 */
public class TestHeader implements Header {
    private final String key;
    private final String value;

    public TestHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getName() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public HeaderElement[] getElements() throws ParseException {
        return new HeaderElement[0];
    }
}
