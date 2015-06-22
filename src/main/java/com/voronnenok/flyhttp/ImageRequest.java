package com.voronnenok.flyhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.voronnenok.flyhttp.cache.NetworkCache;

/**
 * Created by voronnenok on 05.06.15.
 */
public class ImageRequest extends Request<Bitmap> {
    private final int reqWidth;
    private final int reqHeight;

    public ImageRequest(String url, Listener<Bitmap> listener, int reqWidth, int reqHeight) {
        super(url, listener);
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

    @Override
    protected Response<Bitmap> parseNetworkResponse(NetworkResponse networkResponse) {
        Bitmap sampledBitmap = loadSampledBitmap(networkResponse.data, reqWidth, reqHeight);
        Log.d(NetworkCache.TAG, "Bitmap required size " + reqWidth + "x" + reqHeight);
        Log.d(NetworkCache.TAG, "Bitmap loaded   size " + sampledBitmap.getWidth() + "x" + sampledBitmap.getHeight());
        return Response.success(sampledBitmap);
    }

    private static Bitmap loadSampledBitmap(byte[] data, int requiredWidth, int requiredHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        options.inSampleSize = calculateInSampleSize(options, requiredWidth, requiredHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
