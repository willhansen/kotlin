// WITH_STDLIB

fun box(): String {
    return when(konst foo = 42UL) {
        42UL -> "OK"
        else -> "Fail"
    }
}
