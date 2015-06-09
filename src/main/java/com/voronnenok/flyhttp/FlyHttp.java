package com.voronnenok.flyhttp;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.voronnenok.flyhttp.cache.Cache;

/**
 * Created by voronnenok on 01.06.15.
 */
public class FlyHttp implements Dispatcher{
    private static FlyHttp instance;
    public final static int THREADS_COUNT = 1;

    public final Network network;
    public final Delivery delivery;
    public final HttpClient httpClient;
    public final Cache cache;

    public final ExecutorService requestsExecutor = Executors.newFixedThreadPool(THREADS_COUNT);

    private FlyHttp(Cache cache) {
        this.httpClient = new HttpUrlClient();
        this.network = new SmartCachingNetwork(httpClient);
        this.delivery = new SimpleDelivery(new Handler(Looper.getMainLooper()));
        this.cache = cache;
    }

    public synchronized static void init(Cache cache) {
        if(instance == null) {
            instance = new FlyHttp(cache);
        }
    }

    public synchronized static FlyHttp getInstance() {
        return instance;
    }

    @Override
    public void sendRequest(Request<?> request) {
        requestsExecutor.execute(new NetworkTask(request, cache, network, delivery));
    }
}
