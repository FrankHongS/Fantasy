package com.frankhon.fantasymusic.ui.fragments.search

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.application.ServiceLocator
import com.frankhon.fantasymusic.utils.getSystemService
import com.frankhon.fantasymusic.utils.showToast
import com.frankhon.fantasymusic.vo.SimpleSong
import com.frankhon.fantasymusic.vo.event.DownloadCompleteEvent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 * Created by Frank Hon on 2022/10/23 8:51 下午.
 * E-mail: frank_hon@foxmail.com
 */
class SongDownloader(private val context: Context) : DefaultLifecycleObserver {

    private val downloadManager by lazy {
        getSystemService<DownloadManager>(Context.DOWNLOAD_SERVICE)
    }

    private var song: SimpleSong? = null
    private var downloadId = 0L
    private val mainScope by lazy { MainScope() }
    private val localMusicDataSource by lazy {
        ServiceLocator.provideLocalDataSource()
    }
    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == id) {
                showToast(R.string.download_complete)
                val cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    val storedUri =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    song?.let {
                        SongDownloadManager.removeSong(it)
                        mainScope.launch {
                            it.songUri = storedUri
                            EventBus.getDefault()
                                .post(DownloadCompleteEvent(it))
                            localMusicDataSource.insertSong(it)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        context.registerReceiver(
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        context.unregisterReceiver(downloadCompleteReceiver)
        mainScope.cancel()
    }

    fun startDownload(song: SimpleSong) {
        SongDownloadManager.run {
            if (contains(song)) {
                return
            } else {
                addSong(song)
                song.run {
                    if (!songUri.isNullOrEmpty()) {
                        this@SongDownloader.song = song
                        val request = DownloadManager.Request(Uri.parse(songUri))
                            .setTitle(name)
                            .setDescription(context.getString(R.string.downloading))
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true)
                            .setDestinationInExternalFilesDir(
                                context,
                                Environment.DIRECTORY_MUSIC,
                                "$name.mp3"
                            )
                        downloadId = downloadManager.enqueue(request)
                    }
                }
            }
        }
    }

}