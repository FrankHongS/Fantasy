package com.hon.fantasy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * Created by Frank_Hon on 12/2/2019.
 * E-mail: v-shhong@microsoft.com
 */
public class AudioNoisyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            MusicPlayer.getInstance().pause();
        }
    }
}
