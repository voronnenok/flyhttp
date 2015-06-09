package com.voronnenok.flyhttp;

import com.voronnenok.flyhttp.cache.Cache;
import com.voronnenok.flyhttp.errors.FlyError;

/**
 * Created by voronnenok on 01.06.15.
 */
public interface Network {
    public NetworkResponse sendRequest(Request<?> request, Cache.Entry cacheEntry) throws FlyError;
}
