package com.voronnenok.flyhttp;

import org.apache.http.impl.cookie.DateUtils;

import com.voronnenok.flyhttp.cache.Cache;

import java.util.Map;

/**
 * Created by voronnenok on 03.06.15.
 */
public class HeadersParcer {

    public static Cache.Entry parseResponse(NetworkResponse response) {
        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        if(response.headers != null) {
            Map<String, String> headers = response.headers;
            entry.headers = headers;
            String lastMod = headers.get(Headers.LAST_MODIFIED);
            entry.lastModified = getLongTime(lastMod);
            String serverDate = headers.get(Headers.Date);
            entry.serverTime = getLongTime(serverDate);
            entry.eTag = headers.get(Headers.ETag);
        }
        return entry;
    }

    public static long getLongTime(String time) {
        try {
            return DateUtils.parseDate(time).getTime();
        } catch (Exception e) {
            return 0;
        }
    }
}
