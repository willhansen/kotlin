// FIR_IDENTICAL
// KT-54748

sealed class Result<String> {
  data class Success(konst konstue: String) : Result<String>()
  class Failure(konst cause: Throwable) : Result<String>()
}

fun foo(): Result<String> = Result.Success("...")
