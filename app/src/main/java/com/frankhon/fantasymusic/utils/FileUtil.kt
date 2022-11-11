package com.frankhon.fantasymusic.utils

import android.net.Uri
import android.os.Environment
import com.frankhon.fantasymusic.application.Fantasy
import com.frankhon.fantasymusic.vo.db.DBSong
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.hon.mylogger.MyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URI
import java.util.regex.Pattern

/**
 * Created by Frank_Hon on 11/12/2020.
 * E-mail: v-shhong@microsoft.com
 */

fun getSongsFromAssets(): List<DBSong> {
    val context = Fantasy.getAppContext()
    val songs = arrayListOf<DBSong>()
    val config = context.assets.open("config.json")
    val reader = InputStreamReader(config, "utf-8")
    val songObjects = Gson().fromJson(reader, JsonArray::class.java)
    for (i in 0 until songObjects.size()) {
        val song = songObjects[i] as JsonObject
        val name = song.get("name").asString
        val artist = song.get("artist").asString
        val songSrc = context.assets.open(song.get("path").asString)
        val lyricsSrc = song.get("lyrics")?.asString?.let { context.assets.open(it) }
        val dir = File(context.filesDir.absolutePath + File.separator + "songs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val musicFile = File(dir, getMusicFileName(name, artist))
        if (!musicFile.exists()) {
            writeToTargetFile(songSrc, musicFile)
        }
        val lyricsFile = lyricsSrc?.let {
            val file = File(dir, getLyricsFileName(name, artist))
            if (!file.exists()) {
                writeToTargetFile(it, file)
            }
            file
        }
        val dbSong = DBSong(
            name = name,
            artist = artist,
            songUri = Uri.fromFile(musicFile).toString(),
            lyricsUri = lyricsFile?.let { Uri.fromFile(it).toString() }.orEmpty(),
            picUrl = song.get("songPic").asString
        )
        songs.add(dbSong)
    }
    return songs
}

/**
 * 同时删除音频文件和歌词文件
 */
suspend fun deleteFile(song: DBSong): Boolean {
    withContext(Dispatchers.IO) {
        song.run {
            var result = true
            if (songUri.isNotEmpty()) {
                val songFile = File(URI.create(songUri))
                result = result && songFile.delete()
            }
            if (!lyricsUri.isNullOrEmpty()) {
                val lyricsFile = File(URI.create(lyricsUri))
                result = result && lyricsFile.delete()
            }
            return@withContext result
        }
    }
    return false
}

suspend fun writeStringToPath(content: String, path: String?, fileName: String?): File? {
    if (path.isNullOrEmpty() || fileName.isNullOrEmpty()) {
        return null
    }
    return withContext(Dispatchers.IO) {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val target = File(dir, fileName)
        val reader = StringReader(content)
        BufferedWriter(FileWriter(target)).use {
            try {
                val buffer = CharArray(64)
                var count: Int
                while (true) {
                    count = reader.read(buffer)
                    if (count == -1) {
                        break
                    }
                    it.write(buffer, 0, count)
                }
                return@withContext target
            } catch (e: Exception) {
                MyLogger.e(e)
                e.printStackTrace()
                return@withContext null
            } finally {
                reader.close()
            }
        }
    }
}

fun parseLyricsFile(uri: String?): List<Pair<Long, String>>? {
    if (uri.isNullOrEmpty()) {
        return null
    }
    val file = File(URI.create(uri))
    if (!file.exists()) {
        return null
    }
    return BufferedReader(FileReader(file)).use {
        try {
            val lyricsList = mutableListOf<Pair<Long, String>>()
            while (true) {
                val line = it.readLine() ?: break
                var pair = matchesLyrics(line, Pattern.compile("^\\[(.*):(.*)]$"))
                pair?.apply {
                    if (first == "ti") {
                        lyricsList.add(Pair(0L, second))
                    } else if (first == "ar") {
                        lyricsList.add(Pair(0L, second))
                    }
                } ?: run {
                    pair =
                        matchesLyrics(line, Pattern.compile("^\\[(\\d{2}:\\d{2}\\.\\d{2})](.*)$"))
                    pair?.run {
                        lyricsList.add(Pair(transferLyricsTime(first), second))
                    }
                }
            }
            lyricsList
        } catch (e: IOException) {
            null
        }
    }
}

private fun matchesLyrics(line: String, pattern: Pattern): Pair<String, String>? {
    val matcher = pattern.matcher(line)
    while (matcher.find()) {
        val first = matcher.group(1)
        val second = matcher.group(2)
        return Pair(first!!, second!!)
    }
    return null
}

private fun writeToTargetFile(src: InputStream, target: File) {
    BufferedOutputStream(FileOutputStream(target)).use {
        try {
            val buffer = ByteArray(1024 * 1024)
            var count: Int
            while (true) {
                count = src.read(buffer)
                if (count == -1) {
                    break
                }
                it.write(buffer, 0, count)
            }
            it.flush()
        } catch (e: Exception) {
            // do nothing
        }
    }
    src.close()
}

fun getLyricsPath(): String {
    return appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath?.let {
        "$it${File.separator}lyrics"
    } ?: ""
}

fun getLyricsFileName(songName: String?, artist: String?): String {
    if (songName.isNullOrEmpty() && artist.isNullOrEmpty()) {
        return ""
    }
    return "${songName}_$artist.lyrics"
}

fun getMusicFileName(songName: String?, artist: String?): String {
    if (songName.isNullOrEmpty() && artist.isNullOrEmpty()) {
        return ""
    }
    return "${songName}_$artist.mp3"
}
