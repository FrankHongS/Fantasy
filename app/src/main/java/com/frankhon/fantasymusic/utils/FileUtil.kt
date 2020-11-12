package com.frankhon.fantasymusic.utils

import android.content.Context
import android.net.Uri
import com.frankhon.fantasymusic.vo.SimpleSong
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.*

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */
object FileUtil {

    fun getSongsFromAssets(context: Context): List<SimpleSong> {
        val songs = arrayListOf<SimpleSong>()
        val config = context.assets.open("config.json")
        val reader = InputStreamReader(config, "utf-8")
        val songObjects = Gson().fromJson(reader, JsonArray::class.java)
        for (i in 0 until songObjects.size()) {
            val song = songObjects[i] as JsonObject
            val songSrc = context.assets.open(song.get("path").asString)
            val dir = File(context.filesDir.absolutePath + File.separator + "songs")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val tempFile = File(dir, song.get("fileName").asString)
            if (!tempFile.exists()) {
                writeToTargetFile(songSrc, tempFile)
            }
            val simpleSong = SimpleSong(
                name = song.get("name").asString,
                artist = song.get("artist").asString,
                location = Uri.fromFile(tempFile).toString(),
                songPic = song.get("songPic").asString
            )
            songs.add(simpleSong)
        }
        return songs
    }

    fun writeToTargetFile(src: InputStream, target: File) {
        var songOutput: OutputStream? = null
        try {
            songOutput = BufferedOutputStream(FileOutputStream(target))
            val buffer = ByteArray(1024 * 1024)
            var count: Int
            while (true) {
                count = src.read(buffer)
                if (count == -1) {
                    break
                }
                songOutput.write(buffer, 0, count)
            }
            songOutput.flush()
        } catch (e: Exception) {
            // do nothing
        } finally {
            songOutput?.close()
            src.close()
        }
    }

}