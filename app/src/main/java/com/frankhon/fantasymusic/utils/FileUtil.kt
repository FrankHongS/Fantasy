package com.frankhon.fantasymusic.utils

import android.net.Uri
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.vo.SimpleSong
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URI

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */

fun getSongsFromAssets(): List<SimpleSong> {
    val context = Fantasy.getAppContext()
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
            songUri = Uri.fromFile(tempFile).toString(),
            picUrl = song.get("songPic").asString
        )
        songs.add(simpleSong)
    }
    return songs
}

suspend fun deleteFile(song: SimpleSong): Boolean {
    withContext(Dispatchers.IO) {
        song.songUri.takeIf { !it.isNullOrEmpty() }?.let {
            val file = File(URI.create(it))
            return@withContext file.delete()
        }
    }
    return false
}

private fun writeToTargetFile(src: InputStream, target: File) {
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
