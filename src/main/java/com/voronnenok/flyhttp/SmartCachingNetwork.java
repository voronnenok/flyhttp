package com.voronnenok.flyhttp;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.cookie.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.voronnenok.flyhttp.cache.Cache;
import com.voronnenok.flyhttp.errors.FlyError;
import com.voronnenok.flyhttp.errors.ServerError;

/**
 * Created by voronnenok on 01.06.15.
 */
public class SmartCachingNetwork implements Network {
    public final HttpClient client;

    public SmartCachingNetwork(HttpClient client) {
        this.client = client;
    }

    @Override
    public NetworkResponse sendRequest(Request<?> request, Cache.Entry cacheEntry) throws FlyError{
        Map<String, String> additionalHeaders = new HashMap<>();

        if(cacheEntry != null) {
            additionalHeaders.putAll(getCacheHeaders(cacheEntry));
        }

        HttpResponse httpResponse = null;
        try {
            httpResponse = client.fireRequest(request, additionalHeaders);

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            final Map<String, String> headers = convertHeaders(httpResponse.getAllHeaders());

            if (statusCode == HttpStatus.SC_NOT_MODIFIED && cacheEntry != null) {

                return new NetworkResponse(true, cacheEntry.data, statusCode, headers);
            }

            byte[] data = bytesFromEntity(httpResponse.getEntity());
            return new NetworkResponse(false, data, statusCode, headers);

        }catch (IOException e) {

            throw new ServerError("Exception while");
        }


    }

    static Map<String, String> getCacheHeaders(Cache.Entry entry) {
        Map<String, String> cacheHeaders = new HashMap<>();

        if(entry.eTag != null) {
            cacheHeaders.put(Headers.IF_NONE_MATCH, entry.eTag);
        }

        if(entry.lastModified > 0) {
            cacheHeaders.put(Headers.IF_MODIFIED_SINCE, entry.getLastModifiedDate());
        }

        return cacheHeaders;
    }

    static byte[] bytesFromEntity(HttpEntity httpEntity) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {

            inputStream = httpEntity.getContent();

            int bytesRead;
            int POOL_SIZE = 4096;
            byte[] pool = new byte[POOL_SIZE];

            while ((bytesRead = inputStream.read(pool, 0, pool.length)) != -1) {
                outputStream.write(pool, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }catch (IOException io) {

        } finally {

            try {
                outputStream.flush();
                outputStream.close();
                if(inputStream != null) {
                    inputStream.close();
                }
                httpEntity.consumeContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<>(headers.length);

        for(Header header : headers) {
            result.put(header.getName(), header.getValue());
        }

        return result;
    }
}
