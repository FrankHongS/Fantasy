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
import com.frankhon.fantasymusic.media.MediaPlayerManager
import com.frankhon.fantasymusic.vo.PlaySongEvent
import com.frankhon.fantasymusic.vo.SongWrapper
import com.frankhon.simplesearchview.generator.DefaultSearchSuggestionGenerator
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.fragment_search.*
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Frank Hon on 2020-05-19 21:06.
 * E-mail: frank_hon@foxmail.com
 */
private const val SONG_LIST_KEY = "song_list"

class SearchFragment : BaseFragment() {

    private lateinit var searchResultAdapter: SearchResultAdapter

    private var songWrapper: SongWrapper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_search_result.layoutManager = LinearLayoutManager(context)
        searchResultAdapter = SearchResultAdapter(AppExecutors.getInstance()) {
            MediaPlayerManager.getInstance().play(it.url) {
                // do nothing
                EventBus.getDefault().post(PlaySongEvent(false))
            }
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
                            override fun onResponse(call: Call<SongWrapper>, response: Response<SongWrapper>) {
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