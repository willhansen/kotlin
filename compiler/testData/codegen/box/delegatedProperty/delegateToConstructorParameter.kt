// WITH_STDLIB
class C(konst x: String)

class D(c: C) {
    konst x by c::x
}

fun box(): String = D(C("OK")).x
