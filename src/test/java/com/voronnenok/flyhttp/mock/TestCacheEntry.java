package com.voronnenok.flyhttp.mock;

import com.voronnenok.flyhttp.cache.Cache;

import java.util.HashMap;

/**
 * Created by voronnenok on 09.06.15.
 */
public class TestCacheEntry extends Cache.Entry {
    public TestCacheEntry() {
        data = new byte[]{23, 11, 67};
        headers = new HashMap<>();
        headers.put("x-token", "wdfhtfs");
        headers.put("Last-Modified", String.valueOf(System.currentTimeMillis()));
        lastModified = System.currentTimeMillis();
        serverTime = lastModified - 100;
        eTag = "some_tag";
    }
}
