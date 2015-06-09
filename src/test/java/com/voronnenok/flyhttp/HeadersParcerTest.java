package com.voronnenok.flyhttp;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by voronnenok on 08.06.15.
 */
public class HeadersParcerTest {

    @Test
    public void testParseResponse() throws Exception {
        NetworkResponse response = new NetworkResponse(false, new byte[]{1,0,2,3,4,5,2,7}, 200, null);
        Assert.assertNotNull(HeadersParcer.parseResponse(response));
    }

    @Test
    public void testGetLongTime() throws Exception {
        long date = System.currentTimeMillis();
        Assert.assertEquals(0, HeadersParcer.getLongTime(String.valueOf(date)));
    }
}