package com.hon.fantasy.api;

import com.hon.fantasy.api.models.ScrobbleInfo;
import com.hon.fantasy.api.models.UserLoginInfo;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public interface LastFmUserRestService {

    String BASE = "/";

    @POST(BASE)
    @FormUrlEncoded
    void getUserLoginInfo(@Field("method") String method, @Field("format") String format, @Field("api_key") String apikey, @Field("api_sig") String apisig, @Field("username") String username, @Field("password") String password, Callback<UserLoginInfo> callback);

    @POST(BASE)
    @FormUrlEncoded
    void getScrobbleInfo(@Field("api_sig") String apisig, @Field("format") String format, @FieldMap Map<String, String> fields, Callback<ScrobbleInfo> callback);

}

