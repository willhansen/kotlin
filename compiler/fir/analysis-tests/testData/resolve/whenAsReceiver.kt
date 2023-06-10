fun <T, R> T.also(block: () -> R): R {
    return null!!
}

fun foo(b: Boolean, a: Int) {
    konst x = when (b) {
        true -> a
        else -> null
    }?.also {
        1
    }
}
