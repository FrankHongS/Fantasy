package com.frankhon.fantasymusic.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.frankhon.fantasymusic.AppExecutors
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.api.MusicServiceImpl
import com.frankhon.fantasymusic.fragments.BaseFragment
import com.frankhon.fantasymusic.media.MusicPlayer
import com.frankhon.fantasymusic.vo.PlaySongEvent
import com.frankhon.fantasymusic.vo.Song
import com.frankhon.fantasymusic.vo.SongWrapper
import com.frankhon.simplesearchview.generator.DefaultSearchSuggestionGenerator
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.fragment_search.*
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

/**
 * Created by Frank Hon on 2020-05-19 21:06.
 * E-mail: frank_hon@foxmail.com
 */
private const val SONG_LIST_KEY = "song_list"

class SearchFragment : BaseFragment() {

    private lateinit var searchResultAdapter: SearchResultAdapter

    private var songWrapper: SongWrapper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_search_result.layoutManager = LinearLayoutManager(context)
        searchResultAdapter = SearchResultAdapter(AppExecutors.getInstance()) {
            MusicPlayer.getInstance().play(it.url)
            EventBus.getDefault().post(
                PlaySongEvent(
                    isPlaying = true,
                    picUrl = it.album.picUrl,
                    songName = it.name,
                    artistName = it.artists[0].name
                )
            )
        }
        rv_search_result.adapter = searchResultAdapter

        svg_search_songs.setSuggestionGenerator(DefaultSearchSuggestionGenerator(context))
        svg_search_songs.setOnSearchListener {
            MyLogger.d("text: $it")
            MusicServiceImpl.getInstance().findSong(it)
                .enqueue(
                    object : Callback<SongWrapper> {
                        override fun onResponse(
                            call: Call<SongWrapper>,
                            response: Response<SongWrapper>
                        ) {
                            val songWrapper = response.body()
                            songWrapper?.let { updateSongList(it) }
                        }

                        override fun onFailure(call: Call<SongWrapper>, t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                )
        }
        svg_search_songs.setOnBackClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
    }

    private fun getSongsFromRaw(): List<SongWrapper> {
        val inputStream = context!!.assets.open("config.json")
        val songs = Gson().fromJson(InputStreamReader(inputStream, "utf-8"), JsonArray::class.java)
        val target = arrayListOf<SongWrapper>()
        for (i in 0 until songs.size()) {
            val song = songs[i] as JsonObject
            val data = Song()
            data.name = song["name"].asString
            val artists = arrayListOf<Song.Artist>()
            val artist = Song.Artist()
            artist.name = song["artist"].asString
            artists.add(artist)
            data.artists = artists
            val file = File(context!!.getExternalFilesDir(null), "${data.name}.mp3")
            if (!file.exists()) {
                file.mkdirs()
            }
            val outputStream = BufferedOutputStream(FileOutputStream(file))
            val songInputStream = BufferedInputStream(context!!.assets.open(song["url"].asString))
            val buffer = ByteArray(1024 * 1024)
            var count: Int
            while (true) {
                count = songInputStream.read(buffer)
                if (count == -1) {
                    break
                }
                outputStream.write(buffer, 0, count)
            }
            outputStream.flush()
            outputStream.close()
            songInputStream.close()
            data.url = file.absolutePath

            target.add(SongWrapper(100, data))
        }
        return target
    }

    private fun updateSongList(songWrapper: SongWrapper) {
        this.songWrapper = songWrapper
        searchResultAdapter.submitList(listOf(songWrapper.data))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        songWrapper = savedInstanceState?.getParcelable(SONG_LIST_KEY)
        songWrapper?.let { updateSongList(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        songWrapper?.let { outState.putParcelable(SONG_LIST_KEY, it) }
    }
}