enum class E(konst b: Boolean) {
    TRUE(1 == 1)
}

fun box() = if (E.TRUE.b) "OK" else "fail"