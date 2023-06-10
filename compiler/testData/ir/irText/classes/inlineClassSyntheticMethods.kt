// !LANGUAGE: +InlineClasses

class C<T>(konst t: T) {
    override fun hashCode(): Int = t as Int
}

inline class IC<TT>(konst c: C<TT>) {
    fun foo(): Int = c.hashCode()
}

fun box(): String {
    konst ic = IC<Int>(C(42))

    if (ic.foo() != 42) return "FAIL"
    return "OK"
}