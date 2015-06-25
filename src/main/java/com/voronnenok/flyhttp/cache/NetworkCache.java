package com.voronnenok.flyhttp.cache;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by voronnenok on 18.05.15.
 */
public class NetworkCache implements Cache{
    public static final String TAG = NetworkCache.class.getSimpleName();
    private final CacheParams cacheParams;
    private final LruCache<String, Cache.Entry> mMemoryCache;
    private DiskLruCache mDiskCache;
    private volatile boolean isDiscCacheStarting = true;
    private final Object mDiscCacheLock = new Object();

    protected NetworkCache(CacheParams cacheParams) {
        this.cacheParams = cacheParams;
        logEvent("Max memory size " + cacheParams.mMemoryCacheSize);
        mMemoryCache = new LruCache<String, Cache.Entry>(NetworkCache.this.cacheParams.mMemoryCacheSize) {

            @Override
            protected int sizeOf(String key, Cache.Entry entry) {
                int entrySize = entry.size();
                logEvent("Entry memory size " + entrySize);
                return entrySize == 0 ? 1 : entrySize;
            }
        };

        if(cacheParams.mEnableDiscCache && cacheParams.mInitializeDiscCacheOnStart) {
            initDiscCacheAsync();
        }

        logEvent("Network cache created with params : " +
                "\nmemory cache size " + this.cacheParams.mMemoryCacheSize +
                "\ndisc cache size " + this.cacheParams.mDiscCacheSize +
                "\ncache directory " + this.cacheParams.mCacheDirectory);
    }

    public void initDiscCache() {
        synchronized (mDiscCacheLock) {
            if(mDiskCache == null || mDiskCache.isClosed()) {
                File discCacheDir = cacheParams.mCacheDirectory;
                if(cacheParams.mEnableDiscCache) {
                    if (!discCacheDir.exists()) {
                        discCacheDir.mkdirs();
                    }

                    long discCacheSize = cacheParams.mDiscCacheSize;
                    long usableSpace = getUsableSpace(discCacheDir);

                    logEvent("Init usable space " + usableSpace);

                    if(usableSpace > 0 && usableSpace < discCacheSize) {
                        discCacheSize = usableSpace;
                    }

                    try {
                        mDiskCache = DiskLruCache.open(discCacheDir, 1, 1, discCacheSize);
                    } catch (IOException e) {}
                }
            }

            isDiscCacheStarting = false;
            mDiscCacheLock.notifyAll();
        }

        logEvent("Disc cache inited");
    }

    public void initDiscCacheAsync() {
        new DiscCacheAsyncTask().execute(DiscCacheAsyncTask.INIT_CACHE);
    }

    private void waitForDiscCache() {
        while (isDiscCacheStarting) {
            try {
                mDiscCacheLock.wait();
            } catch (InterruptedException e) {}
        }
    }

    public void addEntryToCache(String key, Entry value) {
        if(cacheParams.mEnableMemoryCache) {
            addEntryToMemoryCache(key, value);
        }

        if(cacheParams.mEnableDiscCache) {
            addEntryToDiscCache(key, value);
        }
    }

    private void addEntryToMemoryCache(String key, Entry value) {
        mMemoryCache.put(key, value);
    }

    private void addEntryToDiscCache(String key, Entry value) {
        synchronized (mDiscCacheLock) {
            if(mDiskCache != null) {
                OutputStream out = null;
                try {
                    String hashKey = hashKeyForDisk(key);
                    DiskLruCache.Snapshot snapshot = mDiskCache.get(hashKey);
                    if(snapshot == null) {
                        DiskLruCache.Editor editor = mDiskCache.edit(hashKey);
                        if(editor != null) {
                            logEvent("Try save entry on disc");
                            out = editor.newOutputStream(cacheParams.mDiscCacheIndex);
                            writeEntry(value, out);
                            logEvent("Entry for hashKey " + hashKey + " was written on disc " + value);
                            editor.commit();
                        }
                    } else {
                        snapshot.getInputStream(cacheParams.mDiscCacheIndex).close();
                    }
                } catch (IOException e) {
                    logEvent("Exception occur while puting on disc " + e.getMessage());
                    e.printStackTrace();
                }finally {
                    closeQuitly(out);
                }
            }
        }
    }

    private static void closeQuitly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e){}
    }

    static void writeEntry(Entry entry, OutputStream outputStream) throws IOException{
        DataOutputStream dos = new DataOutputStream(outputStream);
        dos.writeLong(entry.lastModified);
        dos.writeLong(entry.serverTime);
        writeString(entry.eTag, dos);
        writeStringMap(entry.headers, dos);
        writeData(entry.data, dos);
        closeQuitly(dos);
    }

    static void writeString(String string, DataOutputStream dos) throws IOException {
        if(string == null) {
            string = "";
        }
        dos.writeUTF(string);
    }

    static void writeStringMap(Map<String, String> map, DataOutputStream dos) throws IOException{
        if(map != null) {
            dos.writeInt(map.size());
            for (String key : map.keySet()) {
                writeString(key, dos);
                writeString(map.get(key), dos);
            }
        } else {
            dos.writeInt(0);
        }
    }

    static void writeData(byte[] data, DataOutputStream dos) throws IOException {
        dos.writeInt(data.length);
        dos.write(data);
    }

    static Entry readEntry(InputStream inputStream) throws IOException {
        DataInputStream dis = new DataInputStream(inputStream);
        Entry entry = new Entry();
        entry.lastModified = dis.readLong();
        entry.serverTime = dis.readLong();
        entry.eTag = readString(dis);
        entry.headers = readStringMap(dis);
        entry.data = readData(dis);
        closeQuitly(dis);
        return entry;
    }

    static String readString(DataInputStream dis) throws IOException {
        return dis.readUTF();
    }

    static Map<String, String> readStringMap(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        Map<String, String> result = size == 0
                ? Collections.<String, String>emptyMap()
                : new HashMap<String, String>(size);

        for(int current = 0; current < size; current++) {
            String key = readString(dis);
            String value = readString(dis);
            result.put(key, value);
        }

        return result;
    }

    static byte[] readData(DataInputStream dis) throws IOException {
        int size = dis.readInt();
        byte[] buffer = new byte[size];
        int read = dis.read(buffer);
        logEvent("Read " + read + " bytes of data");
        return buffer;
    }

    public Cache.Entry getEntryFromCache(String key) {
        Cache.Entry entry = getEntryFromMemoryCache(key);
        if (entry != null) {
            logEvent(key + " returned from MEMORY cache");
            return entry;
        }

        entry = getEntryFromDiscCache(key);
        if(entry != null) {
            logEvent(key + " returned from DISC cache");
            syncMemoryCache(key, entry);
        } else {
            logEvent(key + " does not exists in cache");
        }

        return entry;
    }

    void syncMemoryCache(String key, Entry entry) {
        if(entry != null && cacheParams.mEnableMemoryCache) {
            addEntryToMemoryCache(key, entry);
        }
    }

    void syncDiscCache(String key, Entry entry) {
        if(entry != null && cacheParams.mEnableDiscCache) {
            addEntryToDiscCache(key, entry);
        }
    }

    private Cache.Entry getEntryFromMemoryCache(String key) {
        Entry entry = mMemoryCache.get(key);
        return mMemoryCache.get(key);
    }

    private Entry getEntryFromDiscCache(String key) {
        synchronized (mDiscCacheLock) {
            waitForDiscCache();

            InputStream inputStream = null;
            Entry value = null;

            try {
                final String hashKey = hashKeyForDisk(key);
                DiskLruCache.Snapshot snapshot = mDiskCache.get(hashKey);
                if(snapshot != null) {
                    inputStream = snapshot.getInputStream(cacheParams.mDiscCacheIndex);
                    value = readEntry(inputStream);
                }
            } catch (IOException e) {
                logEvent("Error occur while retrieving entry from disc");
                e.printStackTrace();
            } finally {
                closeQuitly(inputStream);
            }

            return value;
        }
    }

    public void clearCache() {
        clearMemoryCache();
        clearDiscCache();
    }

    public void clearCacheAsync() {
        new DiscCacheAsyncTask().execute(DiscCacheAsyncTask.CLEAR_CACHE);
    }

    public void clearMemoryCache() {
        mMemoryCache.evictAll();
    }

    public void clearDiscCache() {
        synchronized (mDiscCacheLock) {
            if(mDiskCache != null && !mDiskCache.isClosed()) {
                try {
                    mDiskCache.delete();
                }
                catch (IOException e) {}
                finally {
                    mDiskCache = null;
                }
            }

            initDiscCache();
        }
    }

    public void flushDiscCache() {
        synchronized (mDiscCacheLock) {
            if(mDiskCache != null && !mDiskCache.isClosed()) {
                try {
                    mDiskCache.flush();
                } catch (IOException e) {}
            }
        }
    }

    public void flushDiscCacheAsync() {
        new DiscCacheAsyncTask().execute(DiscCacheAsyncTask.FLUSH_DATA);
    }

    public void closeDiscCache() {
        synchronized (mDiscCacheLock) {
            if(mDiskCache != null && !mDiskCache.isClosed()) {
                try {
                    mDiskCache.close();
                    mDiskCache = null;
                } catch (IOException e) {}
            }
        }
    }

    public void closeDiscCacheAsync() {
        new DiscCacheAsyncTask().execute(DiscCacheAsyncTask.CLOSE_CACHE);
    }

    static long getUsableSpace(File path) {
        StatFs statFs = new StatFs(path.getPath());
        return (long)statFs.getAvailableBlocks() * (long)statFs.getBlockSize();
    }

    static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable()
                        ? getExternalCacheDir(context).getPath()
                        : context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    static File getExternalCacheDir(Context context) {
        if (Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    static boolean isExternalStorageRemovable() {
        if (Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    @Override
    public void put(String key, Entry entry) {
        addEntryToCache(key, entry);
    }

    @Override
    public Entry get(String key) {
        return getEntryFromCache(key);
    }


    protected static class CacheParams {
        private final boolean mEnableMemoryCache;
        private final boolean mEnableDiscCache;
        private final int mMemoryCacheSize;
        private final long mDiscCacheSize;
        private final boolean mInitializeDiscCacheOnStart;
        private final File mCacheDirectory;
        private final int mDiscCacheIndex;

        private CacheParams(Builder builder) {
            mEnableMemoryCache = builder.enableMemoryCache;
            mMemoryCacheSize = builder.memoryCacheSize;
            mEnableDiscCache = builder.enableDiscCache;
            mDiscCacheSize = builder.discCacheSize;
            mInitializeDiscCacheOnStart = builder.initializeDiscCacheOnStart;
            mCacheDirectory = builder.useApplicationCacheDir ? getDiskCacheDir(builder.applicationContext, builder.cacheDirectory)
                                                             : new File(builder.cacheDirectory);
            mDiscCacheIndex = builder.discCacheIndex;
        }
    }

    public static class Builder {
        private static final int DEFAULT_MEMORY_CACHE_SIZE = (int)Runtime.getRuntime().maxMemory() / 4;
        private static final long DEFAULT_DISC_CACHE_SIZE = 1024 * 1024 * 10;
        private static final boolean DEFAULT_MEMORY_CACHE_ENABLED = true;
        private static final boolean DEFAULT_DISC_CACHE_ENABLED = true;
        private static final boolean DEFAULT_DISC_CACHE_AUTOINIT = true;
        private static final int DEFAULT_DISC_CACHE_INDEX = 0;
        private static final String DEFAULT_CACHE_DIRECTORY = "cached images";

        private boolean useApplicationCacheDir = true;
        private int memoryCacheSize = DEFAULT_MEMORY_CACHE_SIZE;
        private boolean enableMemoryCache = DEFAULT_MEMORY_CACHE_ENABLED;
        private long discCacheSize = DEFAULT_DISC_CACHE_SIZE;
        private boolean enableDiscCache = DEFAULT_DISC_CACHE_ENABLED;
        private boolean initializeDiscCacheOnStart = DEFAULT_DISC_CACHE_AUTOINIT;
        private String cacheDirectory = DEFAULT_CACHE_DIRECTORY;
        private int discCacheIndex = DEFAULT_DISC_CACHE_INDEX;
        private final Context applicationContext;

        public Builder(Context context) {
            applicationContext = context == null ? null : context.getApplicationContext();
        }

        public Builder(String cacheDirectory) {
            this.cacheDirectory = cacheDirectory;
            useApplicationCacheDir = false;
            applicationContext  = null;
        }

        public Builder enableMemoryCache(boolean enable) {
            enableMemoryCache = enable;
            return this;
        }

        public Builder setMemoryCacheSize(int cacheSize) {
            memoryCacheSize = cacheSize;
            return this;
        }

        public Builder enableDiscCache(boolean enable) {
            enableDiscCache = enable;
            return this;
        }

        public Builder setDiscCacheSize(int  cacheSize) {
            discCacheSize = cacheSize;
            return this;
        }

        public Builder setCacheDirectory(String directoryName) {
            cacheDirectory = directoryName;
            useApplicationCacheDir = false;
            return this;
        }

        public Builder initCacheImmediatly(boolean init) {
            initializeDiscCacheOnStart = init;
            return this;
        }

        public NetworkCache build() {
            CacheParams cacheParams = new CacheParams(this);
            return new NetworkCache(cacheParams);
        }
    }

    private class DiscCacheAsyncTask extends AsyncTask<Integer, Void, Void> {
        private static final int INIT_CACHE = 1000;
        private static final int FLUSH_DATA = 1001;
        private static final int CLEAR_CACHE = 1002;
        private static final int CLOSE_CACHE = 1003;

        @Override
        protected Void doInBackground(Integer... params) {
            int task = params[0];
            switch (task) {
                case INIT_CACHE:
                    initDiscCache();
                    break;
                case FLUSH_DATA:
                    flushDiscCache();
                    break;
                case CLEAR_CACHE:
                    clearCache();
                    break;
                case CLOSE_CACHE:
                    closeDiscCache();
                    break;
            }
            return null;
        }
    }

    private static void logEvent(String message) {
//        if(BuildConfig.DEBUG) {
            Log.d(TAG, message);
//        }
    }
}
