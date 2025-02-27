// WITH_STDLIB

enum class ResultType constructor(konst reason: String) {
    SOMETHING("123"),
    OK("OK"),
    UNKNOWN("FAIL");

    companion object {
        fun getByVal(reason: String): ResultType {
            return ResultType.konstues().firstOrDefault({ it.reason == reason }, UNKNOWN)
        }
    }
}

inline fun <T> Array<out T>.firstOrDefault(predicate: (T) -> Boolean, default: T): T {
    return firstOrNull(predicate) ?: default
}

fun box(): String = ResultType.getByVal("OK").reason
