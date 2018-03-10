package com.hon.fantasy.api;

import android.content.Context;

import com.hon.fantasy.utils.PreferencesUtility;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public class RestServiceFactory {
    private static final String TAG_OK_HTTP = "OkHttp";
    private static final long CACHE_SIZE = 1024 * 1024;

    public static <T> T createStatic(final Context context, String baseUrl, Class<T> clazz) {
        final OkHttpClient okHttpClient = new OkHttpClient();

        okHttpClient.setCache(new Cache(context.getApplicationContext().getCacheDir(),
                CACHE_SIZE));
        okHttpClient.setConnectTimeout(40, TimeUnit.SECONDS);

        RequestInterceptor interceptor = new RequestInterceptor() {
            PreferencesUtility prefs = PreferencesUtility.getInstance(context);

            @Override
            public void intercept(RequestFacade request) {
                //7-days cache
                request.addHeader("Cache-Control",
                        String.format("max-age=%d,%smax-stale=%d",
                                Integer.valueOf(60 * 60 * 24 * 7),
                                prefs.loadArtistAndAlbumImages() ? "" : "only-if-cached,", Integer.valueOf(31536000)));
                request.addHeader("Connection", "keep-alive");
            }
        };

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setRequestInterceptor(interceptor)
                .setClient(new OkClient(okHttpClient));

        return builder
                .build()
                .create(clazz);

    }

    public static <T> T create(final Context context, String baseUrl, Class<T> clazz) {

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(baseUrl);

        return builder
                .build()
                .create(clazz);

    }
}

