package com.voronnenok.flyhttp.cache;

import org.apache.http.impl.cookie.DateUtils;

import java.util.Date;
import java.util.Map;

/**
 * Created by voronnenok on 24.05.15.
 *
 * Used for saving and retrieving cached server responses for all types of data
 */
public interface Cache {

    /**
     * Put entry in cache
     * @param key Key for {@link Entry} entry
     * @param entry Single {@link Entry} of data
     */
    public void put(String key, Entry entry);

    /**
     * Get single {@link Entry} from cache
     * @param key Key for retrieving {@link Entry}
     * @return Existing {@link Entry} instance for provided key or null otherwise
     */
    public Entry get(String key);

    /**
     * Class wrapping all network response data for holding in {@link Cache}
     */
    public static class Entry {
        /** Abstract body of response */
        public byte[] data;

        /** All response headers */
        public Map<String, String> headers;

        /** Content Last-modified header time(used for caching)*/
        public long lastModified;

        /** The time of last response(used for caching)*/
        public long serverTime;

        /** ETag of response content(used for caching)*/
        public String eTag;

        private int getDataSize() {
            return data != null ? data.length : 0;
        }

        public int size() {
            return getDataSize();
        }

        public String getLastModifiedDate() {
            Date lastModifiedDate = new Date(lastModified);
            return DateUtils.formatDate(lastModifiedDate);
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "\ndata length=" + getDataSize() +
                    ", \nheaders=" + headers +
                    ", \nlastModified=" + lastModified +
                    ", \nserverTime=" + serverTime +
                    ", \neTag='" + eTag + '\'' +
                    '}';
        }
    }

}
