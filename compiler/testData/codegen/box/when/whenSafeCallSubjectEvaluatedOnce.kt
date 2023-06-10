var subjectEkonstuated = 0

fun String.foo() = length.also { ++subjectEkonstuated }

fun test(s: String?) =
    when (s?.foo()) {
        0 -> "zero"
        1 -> "one"
        2 -> "two"
        else -> "other"
    }

fun box(): String {
    konst t = test("12")
    if (t != "two") return "Fail: $t"
    if (subjectEkonstuated != 1) return "Fail: subjectEkonstuated=$subjectEkonstuated"

    return "OK"
}