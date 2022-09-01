package com.frankhon.fantasymusic.media;

import com.danikula.videocache.HttpProxyCacheServer;
import com.frankhon.fantasymusic.application.Fantasy;

/**
 * Created by Frank Hon on 2020-06-06 01:48.
 * E-mail: frank_hon@foxmail.com
 */
public class HttpProxyCache {

    private volatile static HttpProxyCache INSTANCE;

    private HttpProxyCacheServer mHttpProxyCacheServer;

    private HttpProxyCache() {
        mHttpProxyCacheServer = new HttpProxyCacheServer.Builder(Fantasy.getAppContext())
                .maxCacheSize(8 * 1024 * 1024)
                .build();
    }

    public static HttpProxyCache getInstance() {
        if (INSTANCE == null) {
            synchronized (HttpProxyCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpProxyCache();
                }
            }
        }
        return INSTANCE;
    }

    public String getProxyUrl(String url) {
        return mHttpProxyCacheServer.getProxyUrl(url);
    }

    public void shutdown(){
        mHttpProxyCacheServer.shutdown();
    }
}
