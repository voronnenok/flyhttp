package com.voronnenok.flyhttp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.voronnenok.flyhttp.cache.NetworkCache;

/**
 * Created by voronnenok on 25.06.15.
 */
public class RecycleImageView extends ImageView {
    public RecycleImageView(Context context) {
        super(context);
    }

    public RecycleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecycleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecycleImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    int position = -1;
    public void setPosition(int position) {
        Log.d("ImageGridFra", "Change position from " + this.position + " to " + position);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("ImageGridFra", "On attach to window " +position);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("ImageGridFra", "On detach from window " + position);
    }
}
