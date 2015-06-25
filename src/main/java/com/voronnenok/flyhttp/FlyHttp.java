package com.voronnenok.flyhttp;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.voronnenok.flyhttp.cache.Cache;

/**
 * Created by voronnenok on 01.06.15.
 */
public class FlyHttp{
    private static FlyHttp instance;

    final Network network;
    final Delivery delivery;
    final HttpClient httpClient;
    final Cache cache;
    final Dispatcher dispatcher;
    final ImageLoader imageLoader;

    private FlyHttp(Cache cache) {
        this.httpClient = new HttpUrlClient();
        this.network = new SmartCachingNetwork(httpClient);
        this.delivery = new SimpleDelivery(new Handler(Looper.getMainLooper()));
        this.cache = cache;
        dispatcher = new ExecutorDispatcher(network, delivery, httpClient, this.cache);
        imageLoader = new DefaultImageLoader(dispatcher);
    }

    public synchronized static void init(Cache cache) {
        if(instance == null) {
            instance = new FlyHttp(cache);
        }
    }

    public synchronized static FlyHttp getInstance() {
        return instance;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
