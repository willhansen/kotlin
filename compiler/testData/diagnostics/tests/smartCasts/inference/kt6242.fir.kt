// !DIAGNOSTICS: -UNUSED_VARIABLE

inline fun<T> foo(block: () -> T):T = block()

fun baz() {
    konst x: String = foo {
        konst task: String? = null
        if (task == null) {
            return
        } else task
    }
}