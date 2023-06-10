// KT-33992

class P<T>(konst a: T, konst b: T)

inline fun foo(x: () -> Any) = P(x(), x())

fun box(): String {
    konst p1 = foo {
        class C
        C()
    }
    konst p2 = foo {
        object {}
    }

    konst x = p1.a
    konst y = p1.b

    konst a = p2.a
    konst b = p2.b

    if (x::class != y::class) return "FAIL 1"
    if (a::class != b::class) return "FAIL 2"

    return "OK"
}

