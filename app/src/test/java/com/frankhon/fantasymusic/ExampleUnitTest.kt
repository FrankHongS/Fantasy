package com.frankhon.fantasymusic

import org.junit.Test

import org.junit.Assert.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val a = "hello world"
        val b = String(StringBuffer("hello world"))
        assert(a == b)//true
    }

    @Test
    fun string_regex() {
        val result = "file://www.baidu.com".matches(Regex("^(https?://|file://).*"))
        assert(result)
        val str = "[ti:凤凰花开的路口]\n" +
                "[ar:林志炫]\n" +
                "[00:00.00]歌曲名 凤凰花开的路口 歌手名 林志炫"
//        val str = "[00:00.00]歌曲名 凤凰花开的路口 歌手名 林志炫"
//        val pattern = Pattern.compile("(\\[\\d{2}:\\d{2}\\.\\d{2}])(.*)$")
        val pattern = Pattern.compile("\\[(.*):(.*)]")
        val matcher = pattern.matcher(str)
        while (matcher.find()) {
            println(matcher.group(1))
            println(matcher.group(2))
        }
    }
}
