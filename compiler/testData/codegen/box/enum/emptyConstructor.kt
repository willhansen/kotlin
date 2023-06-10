package test

enum class My(konst s: String) {
    ENTRY;
    constructor(): this("OK")
}

fun box() = My.ENTRY.s
