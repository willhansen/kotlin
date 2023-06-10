package test

open class C<T>(v: T) {
    operator fun getValue(p1: Any?, p2: Any?): T = v
}

class A {
    @Suppress("UNRESOLVED_REFERENCE")
    konst x by lazy { Unresolved }
    konst z by C<String>("z")
    konst y by object: C<String>("y") {}
    konst a by lazy { C<String>("a") }
    konst b by lazy { object: C<String>("b") {} }
}
