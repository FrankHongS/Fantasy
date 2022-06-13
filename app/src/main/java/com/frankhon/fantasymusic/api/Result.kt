package com.frankhon.fantasymusic.api

/**
 * Created by Frank Hon on 2022/2/13 8:52 下午.
 * E-mail: frank_hon@foxmail.com
 */
class Result<out T> private constructor(
    val isSuccess: Boolean = false,
    val data: T? = null,
    val errorMessage: String? = ""
) {

    companion object {
        fun <T> success(data: T?): Result<T> {
            return Result(isSuccess = true, data = data)
        }

        fun <T> failure(errorMessage: String?): Result<T> {
            return Result(isSuccess = false, errorMessage = errorMessage)
        }
    }

}