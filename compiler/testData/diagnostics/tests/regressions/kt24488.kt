// SKIP_TXT

class Bar {
    konst a: Array<String>? = null
}

fun foo(bar: Bar) = bar.a?.asIterable() ?: <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>emptyArray<!>()

fun <T> Array<out T>.asIterable(): Iterable<T> = TODO()

fun testFrontend() {
    konst bar = Bar()
    foo(bar)
}
