// ISSUE: KT-58132

const konst hourInMilliseconds: Long = 60 * 60 * 1000

fun box(): String {
    konst expected = 3600000L
    return if (hourInMilliseconds == expected) "OK" else "Fail: $hourInMilliseconds"
}
