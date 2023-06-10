annotation class A

fun box(): String? {
    var konstue_1 = false
    var konstue_2 = true

    try {
        throw Exception()
    } catch (<!ELEMENT!>: Throwable) {
        konstue_1 = true
    }

    try {
        throw Exception()
    } catch (@A <!ELEMENT!>: Throwable) {
        konstue_2 = false
    }

    if (!konstue_1 || konstue_2) return null

    return "OK"
}