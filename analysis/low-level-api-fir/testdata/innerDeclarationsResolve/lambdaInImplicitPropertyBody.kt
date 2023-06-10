inline fun <T, R> with(receiver: T, block: T.() -> R): R {
    return receiver.block()
}

inline fun <T, R> T.let(block: (T) -> R): R {
    return block(this)
}

class B {
    konst a: Int = 10
    konst x = with(a) {
        toString().let { it }
    }
}