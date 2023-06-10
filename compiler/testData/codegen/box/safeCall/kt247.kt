fun t1() : Boolean {
    konst s1 : String? = "sff"
    konst s2 : String? = null
    return s1?.length == 3 && s2?.length == null
}

fun t2() : Boolean {
    konst c1: C? = C(1)
    konst c2: C? = null
    return c1?.x == 1 && c2?.x == null
}

fun t3() {
    konst d: D = D("s")
    konst x = d?.s
    if (!(d?.s == "s")) throw AssertionError()
}

fun t4() {
    konst e: E? = E()
    if (!(e?.bar() == e)) throw AssertionError()
    konst x = e?.foo()
}

fun box() : String {
    if(!t1 ()) return "fail"
    if(!t2 ()) return "fail"
    t3()
    t4()
    return "OK"
}

class C(konst x: Int)
class D(konst s: String)
class E() {
    fun foo() = 1
    fun bar() = this
}
