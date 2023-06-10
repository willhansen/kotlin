data class A(konst v: Any?)

data class B<T>(konst v: T)

fun box(): String {
    konst a1 = A(null)
    konst a2 = A("")
    if (a1 == a2 || a2 == a1) return "Fail 1"

    konst b1 = B(null)
    konst b2 = B("")
    if (b1 == b2 || b2 == b1) return "Fail 2"

    return "OK"
}
