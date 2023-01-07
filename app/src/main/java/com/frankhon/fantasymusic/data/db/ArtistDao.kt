package com.frankhon.fantasymusic.data.db

import androidx.room.Dao
import androidx.room.Query
import com.frankhon.fantasymusic.vo.db.DBArtist

/**
 * Created by shuaihua_a on 2023/1/14 15:08.
 * E-mail: hongshuaihua
 */
@Dao
interface ArtistDao {

    @Query("select song_artist, pic_url, count(*) from songs group by song_artist order by count(*) desc")
    suspend fun getSongInfoByArtist(): List<DBArtist>

}