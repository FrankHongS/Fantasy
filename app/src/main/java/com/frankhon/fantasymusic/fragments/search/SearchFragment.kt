package com.frankhon.fantasymusic.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.frankhon.fantasymusic.AppExecutors
import com.frankhon.fantasymusic.R
import com.frankhon.fantasymusic.api.MusicServiceImpl
import com.frankhon.fantasymusic.fragments.BaseFragment
import com.frankhon.fantasymusic.media.MediaPlayerManager
import com.frankhon.fantasymusic.vo.Song
import com.frankhon.fantasymusic.vo.SongWrapper
import com.hon.mylogger.MyLogger
import kotlinx.android.synthetic.main.fragment_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Frank Hon on 2020-05-19 21:06.
 * E-mail: frank_hon@foxmail.com
 */
class SearchFragment : BaseFragment() {

    private lateinit var searchResultAdapter: SearchResultAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_search_result.layoutManager = LinearLayoutManager(context)
        searchResultAdapter = SearchResultAdapter(AppExecutors.getInstance()){
            MediaPlayerManager.getInstance().play(it.url){
                // do nothing
            }
        }
        rv_search_result.adapter = searchResultAdapter

        et_search_songs.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val text = et_search_songs.text.toString()
                MyLogger.d("text: $text")
                MusicServiceImpl.getInstance().findSong(text)
                    .enqueue(
                        object : Callback<SongWrapper> {
                            override fun onResponse(call: Call<SongWrapper>, response: Response<SongWrapper>) {
                                val songWrapper = response.body()
                                if (songWrapper != null) {
                                    searchResultAdapter.submitList(listOf(songWrapper.data))
                                }
                            }

                            override fun onFailure(call: Call<SongWrapper>, t: Throwable) {
                                t.printStackTrace()
                            }
                        }
                    )
                true
            } else {
                false
            }
        }
    }
}