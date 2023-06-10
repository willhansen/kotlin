// !DIAGNOSTICS: -UNUSED_VARIABLE

class Outer<T> (konst v: T) {
    konst prop: Any?

    init {
        class Inner(konst v: T) {
            override fun toString() = v.toString()
        }

        konst konstue: Inner = Inner(v)
        prop = konstue
    }
}

fun box(): String {
    return Outer("OK").prop.toString()
}
