package com.voronnenok.flyhttp;

/**
 * Created by voronnenok on 01.06.15.
 */
public interface Delivery {
    public void deliverResponse(Request<?> request, Response<?> response);
}
