class A {
    companion object
}

object B {
    konst A.Companion.foo: X get() = X
}

object X

object C {
    operator fun X.invoke() = println("Hello!")
}

inline fun <T, R> with(receiver: T, block: T.() -> R): R = receiver.block()

fun use() = with(C) {
    with(B) {
        A.foo()
    }
}