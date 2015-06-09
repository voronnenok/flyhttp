package com.voronnenok.flyhttp;

import com.voronnenok.flyhttp.errors.FlyError;

/**
 * Created by voronnenok on 26.05.15.
 */
public class Response<Result> {
    public final Result data;
    public final FlyError error;

    public Response(Result data) {
        this.data = data;
        this.error = null;
    }

    public Response(FlyError error) {
        this.error = error;
        this.data = null;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public static <Result> Response<Result> success(Result response) {
        return new Response<>(response);
    }

    public static <Result> Response<Result> error(FlyError error) {
        return new Response<>(error);
    }
}
