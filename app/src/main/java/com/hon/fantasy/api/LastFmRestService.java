package com.hon.fantasy.api;

import com.hon.fantasy.api.models.AlbumInfo;
import com.hon.fantasy.api.models.ArtistInfo;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public interface LastFmRestService {

    String BASE_PARAMETERS_ALBUM = "/?method=album.getinfo&api_key=a24b14bba69d8e67d8a0aaba15d87f4d&format=json";
    String BASE_PARAMETERS_ARTIST = "/?method=artist.getinfo&api_key=a24b14bba69d8e67d8a0aaba15d87f4d&format=json";

    @Headers("Cache-Control: public")
    @GET(BASE_PARAMETERS_ALBUM)
    void getAlbumInfo(@Query("artist") String artist, @Query("album") String album, Callback<AlbumInfo> callback);

    @Headers("Cache-Control: public")
    @GET(BASE_PARAMETERS_ARTIST)
    void getArtistInfo(@Query("artist") String artist, Callback<ArtistInfo> callback);

}

