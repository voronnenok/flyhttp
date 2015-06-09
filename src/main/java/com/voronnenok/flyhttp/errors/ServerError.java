package com.voronnenok.flyhttp.errors;

/**
 * Created by voronnenok on 01.06.15.
 */
public class ServerError extends FlyError {

    public ServerError(String detailMessage) {
        super(detailMessage);
    }

    public ServerError(Throwable throwable) {
        super(throwable);
    }
}
