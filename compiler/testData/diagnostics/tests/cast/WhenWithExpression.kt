// FIR_IDENTICAL
class List<out T>(konst size : Int) {
    companion object {
        konst Nil = List<Nothing>(0)
    }
}

fun List<String>.join() =
        when (this) {
            List.Nil -> "[]" // CANNOT_CHECK_FOR_ERASED was reported
            else -> ""
        }
