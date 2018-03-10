package com.hon.fantasy.cast;

import android.content.Context;
import android.net.Uri;

import com.hon.fantasy.utils.Constants;
import com.hon.fantasy.utils.FantasyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class WebServer extends NanoHTTPD {

    private Context context;
    private Uri songUri, albumArtUri;

    public WebServer(Context context) {
        super(Constants.CAST_SERVER_PORT);
        this.context = context;
    }

    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files) {
        if (uri.contains("albumart")) {
            //serve the picture

            String albumId = parameters.get("id");
            this.albumArtUri = FantasyUtils.getAlbumArtUri(Long.parseLong(albumId));

            if (albumArtUri != null) {
                String mediasend = "image/jpg";
                InputStream fisAlbumArt = null;
                try {
                    fisAlbumArt = context.getContentResolver().openInputStream(albumArtUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Response.Status st = Response.Status.OK;

                //serve the song
                return newChunkedResponse(st, mediasend, fisAlbumArt);
            }

        } else if (uri.contains("song")) {

            String songId = parameters.get("id");
            this.songUri = FantasyUtils.getSongUri(context, Long.parseLong(songId));

            if (songUri != null) {
                String mediasend = "audio/mp3";
                FileInputStream fisSong = null;
                File song = new File(songUri.getPath());
                try {
                    fisSong = new FileInputStream(song);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Response.Status st = Response.Status.OK;

                //serve the song
                return newFixedLengthResponse(st, mediasend, fisSong, song.length());
            }

        }
        return newFixedLengthResponse("Error");
    }

}
