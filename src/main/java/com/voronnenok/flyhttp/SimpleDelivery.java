package com.voronnenok.flyhttp;

import android.os.Handler;
import java.util.concurrent.Executor;

/**
 * Created by voronnenok on 03.06.15.
 */
public class SimpleDelivery implements Delivery {
    private final Executor executor;

    public SimpleDelivery(final Handler handler) {
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    @Override
    public void deliverResponse(Request<?> request, Response<?> response) {
        executor.execute(new DeliveryTask(request, response));
    }

    private static class DeliveryTask implements Runnable {
        private final Request request;
        private final Response response;

        public DeliveryTask(Request<?> request, Response<?> response) {
            this.request = request;
            this.response = response;
        }


        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            if(!request.isCanceled()) {
                request.deliverResponse(response);
            }
        }
    }
}
