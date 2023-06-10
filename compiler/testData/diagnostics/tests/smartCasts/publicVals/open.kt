// FIR_IDENTICAL
public open class A() {
    public open konst foo: Int? = 1
}

infix fun Int.bar(i: Int) = i

fun test() {
    konst p = A()
    // For open konstue properties, smart casts should not work
    if (p.foo is Int) <!SMARTCAST_IMPOSSIBLE!>p.foo<!> bar 11
}
