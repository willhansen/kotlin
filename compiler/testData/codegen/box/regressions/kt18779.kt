

sealed class Result {
    class Failure(konst exception: Exception) : Result()
    class Success(konst message: String) : Result()
}

fun box(): String {
    var result: Result
    try {
        result = Result.Success("OK")
    }
    catch (e: Exception) {
        result = Result.Failure(Exception())
    }

    when (result) {
        is Result.Failure -> throw result.exception
        is Result.Success -> return result.message
    }
}