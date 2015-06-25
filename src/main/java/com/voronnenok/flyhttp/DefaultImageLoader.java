package com.voronnenok.flyhttp;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.voronnenok.flyhttp.cache.NetworkCache;
import com.voronnenok.flyhttp.errors.FlyError;

import java.lang.ref.WeakReference;

/**
 * Created by voronnenok on 24.06.15.
 */
public class DefaultImageLoader implements ImageLoader{

    private final Dispatcher requestDispatcher;
//    private final Map<String, String> tasksQueue

    public DefaultImageLoader(Dispatcher requestDispatcher) {
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void loadImage(ImageView view, String url) {
        loadImage(view, url, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void loadImage(ImageView view, String url, int width, int height) {
        ImageTask imageTask = new ImageTask(view, url, width, height, requestDispatcher);
        imageTask.start();
    }



    private static class ImageTask implements Request.Listener<Bitmap>, View.OnAttachStateChangeListener{
        final WeakReference<ImageView> imageView;
        final String url;
        final int width;
        final int height;
        final Dispatcher dispatcher;
        final ImageRequest request;
        boolean isViewAttached = true;

        public ImageTask(ImageView imageView, String url, int width, int height, Dispatcher dispatcher) {
            this.imageView = new WeakReference<>(imageView);
            this.url = url;
            this.width = width;
            this.height = height;
            this.dispatcher = dispatcher;
            request = new ImageRequest(url, this, width, height);
        }

        public void start() {
            ImageView image = imageView.get();
            image.addOnAttachStateChangeListener(this);
            dispatcher.sendRequest(request);
        }

        @Override
        public void onSuccess(Bitmap response) {
            ImageView view = imageView.get();
            if(view != null && isViewAttached) {
                view.setImageBitmap(response);
            } else {
                response.recycle();
            }
        }

        @Override
        public void onError(FlyError error) {
            Log.d(NetworkCache.TAG, "Error getting image");
        }

        @Override
        public void onViewAttachedToWindow(View v) {
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            isViewAttached = false;
            request.cancel();
        }
    }
}
