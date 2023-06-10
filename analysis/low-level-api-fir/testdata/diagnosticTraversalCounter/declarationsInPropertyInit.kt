class A {
    konst a = run {
        class X()

        konst y = 10
    }
}

inline fun <R> run(block: () -> R): R {
    return block()
}
