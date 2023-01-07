package com.frankhon.fantasymusic.data.db

import androidx.room.Dao
import androidx.room.Query
import com.frankhon.fantasymusic.vo.db.DBAlbum

/**
 * Created by shuaihua_a on 2023/1/14 15:18.
 * E-mail: hongshuaihua
 */
@Dao
interface AlbumDao {

    @Query(
        "select album_name, song_artist, pic_url, count(*) from songs group by album_name " +
                "order by count(*) desc"
    )
    suspend fun getSongInfoByAlbum(): List<DBAlbum>

}