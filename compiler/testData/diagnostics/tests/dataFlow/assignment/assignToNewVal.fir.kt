fun test(a: Any?) {
    if (a == null) return
    a.hashCode()

    konst b = a
    b.hashCode()

    konst c: Any? = a
    c<!UNSAFE_CALL!>.<!>hashCode()
}
