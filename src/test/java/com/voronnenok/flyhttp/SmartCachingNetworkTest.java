package com.voronnenok.flyhttp;

import com.voronnenok.flyhttp.cache.Cache;
import com.voronnenok.flyhttp.mock.TestHeader;
import com.voronnenok.flyhttp.mock.TestResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by voronnenok on 15.06.15.
 */
public class SmartCachingNetworkTest {

    @Test
    public void testSendRequest() throws Exception {
        HttpClient client = mock(HttpClient.class);
        Header[] testHeaders = new Header[2];
        testHeaders[0] = new TestHeader("Accept-Encoding", "UTF-8");
        testHeaders[1] = new TestHeader("X-token", "testToken");
        byte[] data = new byte[]{10,11,55,56,32,48,2,45,86,24,45,32,15,6,};
        ByteArrayInputStream is = new ByteArrayInputStream(data);

        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(is);

        BasicHttpResponse response = new TestResponse(200, entity, testHeaders);

        when(client.fireRequest(any(Request.class), anyMapOf(String.class, String.class))).thenReturn(response);

        Network network = new SmartCachingNetwork(client);
        NetworkResponse networkResponse = network.sendRequest(new TestRequest(), null);
        assertArrayEquals(data, networkResponse.data);

        assertEquals(testHeaders.length, networkResponse.headers.size());
        for(Header header : testHeaders) {
            assertEquals(header.getValue(), networkResponse.headers.get(header.getName()));
        }
    }

    @Test
    public void testGetCacheHeaders() throws Exception {
        Cache.Entry entry = mock(Cache.Entry.class);
        when(entry.getLastModifiedDate()).thenReturn(String.valueOf(entry.lastModified));
        entry.lastModified = System.currentTimeMillis();
        entry.eTag = "objectHash";

        Map<String, String> cacheHeaders = SmartCachingNetwork.getCacheHeaders(entry);
        assertTrue(cacheHeaders.containsKey("If-Modified-Since"));
        assertTrue(cacheHeaders.containsKey("If-None-Match"));
    }

    @Test
    public void testBytesFromByteArrayEntity() throws Exception {
        byte[] testData = new byte[]{11,44,53,55,3,12,8,0,1,5,0,6,8,43,0,48,5,0,4,54,56,54,5,45,54,54,2,8,1,15,5,1,9,7,7,4,5,52,86,3,2,44,8,2,5,4};
        ByteArrayInputStream is = new ByteArrayInputStream(testData);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(is);

        byte[] retrievedData = SmartCachingNetwork.bytesFromEntity(entity);
        assertArrayEquals(testData, retrievedData);
    }

    @Test
    public void testBytesFromImageFileEntity() throws Exception {
        File testFile = new File("src/test/resources/files/test_image.jpg");
        FileInputStream inputStream = new FileInputStream(testFile);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(inputStream);

        byte[] retrievedData = SmartCachingNetwork.bytesFromEntity(entity);
        assertEquals(testFile.length(), retrievedData.length);
    }

    @Test
    public void testConvertHeaders() throws Exception {
        List<Header> listHeaders = new ArrayList<>();
        listHeaders.add(new TestHeader("Content-Type", "application/json, utf-8"));
        listHeaders.add(new TestHeader("Content-Length", "1235485"));
        listHeaders.add(new TestHeader("Last-Modified", "Thu, 15 Nov 2007 00:00:00 GMT"));
        listHeaders.add(new TestHeader("cache-control", "full"));
        Map<String, String> mapHeaders = SmartCachingNetwork.convertHeaders(listHeaders.toArray(new Header[listHeaders.size()]));

        assertEquals(listHeaders.size(), mapHeaders.size());
        for(Header header : listHeaders) {
            assertEquals(header.getValue(), mapHeaders.get(header.getName()));
        }
    }
}