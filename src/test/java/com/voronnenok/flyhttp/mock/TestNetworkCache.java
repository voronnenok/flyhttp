package com.voronnenok.flyhttp.mock;

import android.content.Context;
import com.voronnenok.flyhttp.cache.NetworkCache;

import org.mockito.Mock;

import java.io.File;

/**
 * Created by voronnenok on 09.06.15.
 */
public class TestNetworkCache extends NetworkCache {

    private TestNetworkCache(NetworkCache.CacheParams cacheParams) {
        super(cacheParams);
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        return new File(uniqueName);
    }
}
