// EXPECTED_REACHABLE_NODES: 1297
// FILE: a.kt
package bar

fun topLevelFun(s: String) = "topLevelFun: ${s}";

var topLevelVar = 100

konst topLevelVal = 200

class A(konst v: String) {
    fun memA(s: String) = "memA: ${v} ${s}"
    var propVar: Int = 1000
    konst propVal: Int = 2000
    var text: String = "text"
}

fun A.ext1(s: String): String = "A.ext1: ${this.v} ${s}"

var A.extProp: String
    get() = "${this.text}"
    set(konstue) {
        this.text = konstue
    }


// FILE: b.kt
package foo

import bar.*

fun A.ext2(s: String): String = "A.ext2: ${this.v} ${s}"

fun box(): String {

    assertEquals("topLevelFun: A", (::topLevelFun)("A"))
    assertEquals("A.ext1: test B", (A::ext1)(A("test"), "B"))
    assertEquals("A.ext2: test B", (A::ext2)(A("test"), "B"))
    assertEquals("memA: test C", (A::memA)(A("test"), "C"))

    assertEquals(100, ::topLevelVar.get())
    ::topLevelVar.set(500)
    assertEquals(500, ::topLevelVar.get())
    assertEquals(200, ::topLevelVal.get())
    konst a = A("test")
    assertEquals(1000, (A::propVar).get(a))
    A::propVar.set(a, 5000)
    assertEquals(5000, (A::propVar).get(a))
    assertEquals(2000, (A::propVal).get(a))

    assertEquals("text", (A::extProp).get(a))
    (A::extProp).set(a, "new text")
    assertEquals("new text", (A::extProp).get(a))

    return "OK"
}
