package com.voronnenok.flyhttp;

import com.voronnenok.flyhttp.cache.Cache;
import com.voronnenok.flyhttp.errors.FlyError;

/**
 * Created by voronnenok on 01.06.15.
 */
public class NetworkTask implements Runnable{

    private final Request<?> request;
    private final Cache cache;
    private final Network network;
    private final Delivery delivery;

    public NetworkTask(Request<?> request, Cache cache, Network network, Delivery delivery) {
        this.request = request;
        this.cache = cache;
        this.network = network;
        this.delivery = delivery;
        assert this.request != null && this.cache != null && this.network != null && this.delivery != null;
    }

    @Override
    public void run() {
        final Cache.Entry entry = cache.get(request.getUrl());

        NetworkResponse networkResponse;
        Response<?> response;
        try {
            networkResponse = network.sendRequest(request, entry);
            if(networkResponse.notModified) {
                networkResponse.data = entry.data;
            } else {
                cache.put(request.getUrl(), HeadersParcer.parseResponse(networkResponse));
            }
            response = request.parseNetworkResponse(networkResponse);
        } catch (FlyError error) {
            response = Response.error(error);
        }

        delivery.deliverResponse(request, response);

    }

}
