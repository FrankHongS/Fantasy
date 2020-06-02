package com.frankhon.fantasymusic.api;

import com.frankhon.fantasymusic.BuildConfig;
import com.frankhon.fantasymusic.vo.Song;
import com.frankhon.fantasymusic.vo.SongWrapper;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Frank Hon on 2020-06-01 23:56.
 * E-mail: frank_hon@foxmail.com
 */
public final class MusicServiceImpl {

    private static final String BASE_URL = "http://api.migu.jsososo.com/";

    private static volatile MusicServiceImpl INSTANCE;

    private MusicService mMusicService;

    private MusicServiceImpl() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        mMusicService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()
                .create(MusicService.class);
    }

    public static MusicServiceImpl getInstance() {
        if (INSTANCE == null) {
            synchronized (MusicServiceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MusicServiceImpl();
                }
            }
        }

        return INSTANCE;
    }

    public Call<SongWrapper> findSong(String keyword) {
        return mMusicService.findSong(keyword);
    }

}
