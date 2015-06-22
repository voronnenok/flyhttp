package com.voronnenok.flyhttp;

import com.voronnenok.flyhttp.cache.Cache;
import com.voronnenok.flyhttp.mock.TestCacheEntry;
import com.voronnenok.flyhttp.mock.TestRequest;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created by voronnenok on 22.06.15.
 */
public class NetworkTaskTest {

    /**
     * Should deliver response data from cache
     */
    @Test
    public void testRun() throws Exception {
        Cache.Entry entry = new TestCacheEntry();
        Request<Object> request = spy(new TestRequest());
        Cache cache = mock(Cache.class);
        when(cache.get(anyString())).thenReturn(entry);
        Network network = mock(Network.class);
        NetworkResponse networkResponse = new NetworkResponse(false, new byte[]{1,4,2,6,2}, 304, null);
        when(network.sendRequest(request, entry)).thenReturn(networkResponse);
        Delivery delivery = mock(Delivery.class);

        NetworkTask networkTask = new NetworkTask(request, cache, network, delivery);
        networkTask.run();
        verify(request).parseNetworkResponse(networkResponse);
        verify(delivery).deliverResponse(eq(request), any(Response.class));
    }
}