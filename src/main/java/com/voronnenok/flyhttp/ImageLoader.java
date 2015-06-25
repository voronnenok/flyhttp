package com.voronnenok.flyhttp;

import android.widget.ImageView;

/**
 * Created by voronnenok on 24.06.15.
 */
public interface ImageLoader {
    public void loadImage(ImageView view, String url);
    public void loadImage(ImageView view, String url, int width, int height);
}
