// WITH_STDLIB
// ISSUE: KT-32462

fun decodeValue(konstue: String): Any {
    return when (konstue[0]) {
        'F' -> String::toFloat
        'B' -> String::toBoolean
        'I' -> String::toInt
        else -> throw IllegalArgumentException("Unexpected konstue prefix: ${konstue[0]}")
    }(konstue.substring(2))
}

fun box(): String = "OK"
