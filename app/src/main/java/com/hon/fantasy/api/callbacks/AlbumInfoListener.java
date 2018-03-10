package com.hon.fantasy.api.callbacks;

import com.hon.fantasy.api.models.LastfmAlbum;

/**
 * Created by Frank on 2018/3/3.
 * E-mail:frank_hon@foxmail.com
 */

public interface AlbumInfoListener {

    void albumInfoSuccess(LastfmAlbum album);

    void albumInfoFailed();

}

