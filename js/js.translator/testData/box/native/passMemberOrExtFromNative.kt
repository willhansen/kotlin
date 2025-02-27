// EXPECTED_REACHABLE_NODES: 1285
package foo

external class A(v: String) {
    konst v: String

    fun m(i:Int, s:String): String = definedExternally
}

fun bar(a: A, extLambda: A.(Int, String) -> String): String = a.(extLambda)(4, "boo")

fun box(): String {
    konst a = A("test")

    assertEquals("A.m test 4 boo", a.m(4, "boo"))
    assertEquals("A.m test 4 boo", bar(a, fun A.(i, s) = (A::m)(this, i, s)))

    return "OK"
}
