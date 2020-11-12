package com.frankhon.fantasymusic.media;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.frankhon.fantasymusic.Fantasy;
import com.hon.mylogger.MyLogger;

import java.io.IOException;

import static android.media.AudioManager.*;

/**
 * Created by Frank_Hon on 3/11/2019.
 * E-mail: v-shhong@microsoft.com
 */
public class MediaPlayerManager {

    private static MediaPlayerManager sInstance;

    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private HttpProxyCache mHttpProxyCache;

    private State mPlayerState = State.IDLE;

    private MediaPlayerManager() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            mp.reset();
            return false;
        });
        mAudioManager = (AudioManager) Fantasy.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        mOnAudioFocusChangeListener = focusChange -> {
            if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
                pause();
            } else if (focusChange == AUDIOFOCUS_GAIN) {
                resume();
            } else if (focusChange == AUDIOFOCUS_LOSS) {
                release();
            }
        };
    }

    public static MediaPlayerManager getInstance() {
        if (sInstance == null) {
            synchronized (MediaPlayerManager.class) {
                if (sInstance == null) {
                    sInstance = new MediaPlayerManager();
                }
            }
        }

        return sInstance;
    }

    public int getDuration(String audioFilePath) throws IOException {
        mMediaPlayer.setDataSource(audioFilePath);
        mMediaPlayer.prepare();
        int duration = mMediaPlayer.getDuration() / 1000;
        mMediaPlayer.reset();
        return duration;
    }

    public void play(String audioFilePath, MediaPlayer.OnCompletionListener onCompletionListener) {
        int result = requestAudioFocus();
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            try {

                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnCompletionListener(mp -> {
                    mPlayerState = State.STOPPED;
                    onCompletionListener.onCompletion(mp);
                });
                mMediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    mPlayerState = State.PLAYING;
                });
                if (audioFilePath.startsWith("file://")) {
                    mMediaPlayer.setDataSource(audioFilePath);
                } else {
                    if (mHttpProxyCache == null) {
                        mHttpProxyCache = HttpProxyCache.getInstance();
                    }
                    mMediaPlayer.setDataSource(mHttpProxyCache.getProxyUrl(audioFilePath));
                }
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                // do nothing
            }
        } else {
            MyLogger.e("Error to request playing: " + result);
            Toast.makeText(Fantasy.getAppContext(), "请求播放失败", Toast.LENGTH_SHORT).show();
        }

    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayerState = State.PAUSED;
//            abandonAudioFocus();
        }
    }

    public void resume() {
        switch (mPlayerState) {
            case IDLE:
                break;
            case PAUSED:
            case STOPPED:
                int result = requestAudioFocus();
                if (result == AUDIOFOCUS_REQUEST_GRANTED) {
                    mMediaPlayer.start();
                }
                break;
            default:
                break;
        }
    }

    public void release() {
        mMediaPlayer.release();
        abandonAudioFocus();
        if (mHttpProxyCache != null) {
            mHttpProxyCache.shutdown();
        }
    }

    private int requestAudioFocus() {
        return mAudioManager.requestAudioFocus(
                mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );
    }

    @SuppressWarnings("UnusedReturnValue")
    private int abandonAudioFocus() {
        return mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
    }

    private enum State {
        IDLE,
        PLAYING,
        PAUSED,
        STOPPED
    }
}
