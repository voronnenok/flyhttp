package com.voronnenok.flyhttp.cache;

import com.voronnenok.flyhttp.mock.TestCacheEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by voronnenok on 10.06.15.
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkCacheTest {


    @Test
    public void testHashKeyForDisk() throws Exception {
        String key1 = "http://test.com/sports";
        String hashKey1 = NetworkCache.hashKeyForDisk(key1);
        assertNotNull(hashKey1);
        assertTrue(hashKey1.length() > 0);
        assertNotEquals(key1, hashKey1);
        assertFalse(hashKey1.contains(":"));
        assertFalse(hashKey1.contains("/"));
        assertEquals(hashKey1, NetworkCache.hashKeyForDisk(key1));
        String key2 = "http://test.com/Sports";
        String hashKey2 = NetworkCache.hashKeyForDisk(key2);
        assertNotEquals(hashKey1, hashKey2);
        assertEquals(hashKey1.length(), hashKey2.length());
    }

    @Test
    public void testSerializeString() throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        String empty = "";
        String notEmpty = "text2349/sd:f";
        NetworkCache.writeString(empty, out);
        NetworkCache.writeString(notEmpty, out);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(empty, NetworkCache.readString(in));
        assertEquals(notEmpty, NetworkCache.readString(in));
    }

    @Test
    public void testSerializeByteData() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        byte[] empty = new byte[0];
        byte[] test = new byte[]{1,11,5,22,46,34,77};
        NetworkCache.writeData(empty, out);
        NetworkCache.writeData(test, out);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(empty.length, NetworkCache.readData(in).length);
        byte[] cachedTest = NetworkCache.readData(in);
        assertEquals(test.length, cachedTest.length);
        for(int i = 0; i < test.length; i++) {
            assertEquals(test[i], cachedTest[i]);
        }
    }

    @Test
    public void testSerializeMap() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        Map<String, String> empty = new HashMap<>();
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> emptyMap = new HashMap<>();
        empty.put("", "testValue");
        map1.put("testKey", "123456789");
        map1.put("someKey", "fdgdobm");
        NetworkCache.writeStringMap(empty, out);
        NetworkCache.writeStringMap(map1, out);
        NetworkCache.writeStringMap(emptyMap, out);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(empty, NetworkCache.readStringMap(in));
        Map<String, String> cache1 = NetworkCache.readStringMap(in);
        assertEquals(map1.size(), cache1.size());
        for(String key : map1.keySet()) {
            assertTrue(cache1.containsKey(key));
            assertEquals(map1.get(key), cache1.get(key));
        }
        assertEquals(emptyMap.size(), NetworkCache.readStringMap(in).size());
    }

    @Test
    public void testWriteReadEntry() throws Exception{
        Cache.Entry entry = new TestCacheEntry();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        NetworkCache.writeEntry(entry, outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Cache.Entry cachedEntry = NetworkCache.readEntry(inputStream);
        assertEquals(entry.size(), cachedEntry.size());
        assertEquals(entry.headers.size(), cachedEntry.headers.size());
        for(String key : entry.headers.keySet()) {
            assertTrue(cachedEntry.headers.containsKey(key));
            assertEquals(entry.headers.get(key), cachedEntry.headers.get(key));
        }
        assertEquals(entry.lastModified, cachedEntry.lastModified);
        assertEquals(entry.serverTime, cachedEntry.serverTime);
        assertEquals(entry.eTag, cachedEntry.eTag);
    }

}