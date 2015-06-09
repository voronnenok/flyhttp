package com.voronnenok.flyhttp;

/**
 * Created by voronnenok on 01.06.15.
 */
public interface Dispatcher {
    public void sendRequest(Request<?> request);
}
