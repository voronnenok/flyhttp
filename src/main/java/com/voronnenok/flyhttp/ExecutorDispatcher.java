package com.voronnenok.flyhttp;

import com.voronnenok.flyhttp.cache.Cache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by voronnenok on 24.06.15.
 */
public class ExecutorDispatcher implements Dispatcher {

    public final static int THREADS_COUNT = 4;
    public final ExecutorService requestsExecutor = Executors.newFixedThreadPool(THREADS_COUNT);
    public final Network network;
    public final Delivery delivery;
    public final HttpClient httpClient;
    public final Cache cache;

    public ExecutorDispatcher(Network network, Delivery delivery, HttpClient httpClient, Cache cache) {
        this.network = network;
        this.delivery = delivery;
        this.httpClient = httpClient;
        this.cache = cache;
    }

    @Override
    public void sendRequest(Request<?> request) {
        requestsExecutor.execute(new NetworkTask(request, cache, network, delivery));
    }
}
