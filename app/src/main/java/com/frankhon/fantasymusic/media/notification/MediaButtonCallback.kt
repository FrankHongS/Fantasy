package com.frankhon.fantasymusic.media.notification

import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import com.frankhon.fantasymusic.media.AudioPlayer
import com.hon.mylogger.MyLogger

/**
 * 耳机按钮点击事件回调，以及通知栏点击事件回调
 *
 * Created by shuaihua_a on 2023/1/5 11:45.
 * E-mail: hongshuaihua
 */
class MediaButtonCallback : MediaSessionCompat.Callback() {

    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
        if (Build.VERSION.SDK_INT >= 27) {
            val keyEvent =
                mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            //此处需要屏蔽ACTION_UP，否则会接收到两次相同keyCode事件
            if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) {
                return false
            }
            MyLogger.d("onMediaButtonEvent: ${keyEvent.keyCode}")
            when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE -> AudioPlayer.toggle()
                KeyEvent.KEYCODE_MEDIA_NEXT -> AudioPlayer.next()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> AudioPlayer.previous()
                else -> return false
            }
            return true
        } else {
            return super.onMediaButtonEvent(mediaButtonEvent)
        }
    }

    override fun onPlay() {
        AudioPlayer.resume()
    }

    override fun onPause() {
        AudioPlayer.pause()
    }

    override fun onSkipToNext() {
        AudioPlayer.next()
    }

    override fun onSkipToPrevious() {
        AudioPlayer.previous()
    }

}