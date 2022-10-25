package com.frankhon.fantasymusic

import org.junit.Test

import org.junit.Assert.*

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
    }
}
