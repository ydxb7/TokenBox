package ai.tomorrow.tokenbox.utils

sealed class Result<R, E: Exception>{
    class Success<R, E: Exception>(val value: R): Result<R, E>()
    class Failure<R, E: Exception>(val error: E): Result<R, E>()
}