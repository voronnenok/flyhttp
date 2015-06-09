package com.voronnenok.flyhttp.errors;

/**
 * Created by voronnenok on 26.05.15.
 */
public class FlyError extends Exception{

    public FlyError(String detailMessage) {
        super(detailMessage);
    }

    public FlyError(Throwable throwable) {
        super(throwable);
    }
}
