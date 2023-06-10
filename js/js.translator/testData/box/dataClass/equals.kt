// EXPECTED_REACHABLE_NODES: 1312
package foo

data class Holder<T>(konst v: T)

data class Dat(konst start: String, konst end: String)

data class Dat2(konst start: String, konst end: String)

class Obj(konst start: String, konst end: String)

fun box(): String {
    konst d1 = Dat("a", "b")
    konst d2 = Dat("a", "b")
    konst d3 = Dat("a", "c")

    konst otherD1 = Dat2("a", "b")

    assertEquals(d1, d1)
    assertEquals(d1, d2)
    assertNotEquals(d1, d3)

    assertNotEquals(d1, otherD1)

    var hd1 = Holder(Dat("y", "n"))
    var hd2 = Holder(Dat("y", "n"))
    var hd3 = Holder(Dat("1", "2"))

    assertEquals(hd1, hd1)
    assertEquals(hd1, hd2)
    assertNotEquals(hd1, hd3)

    var ho1 = Holder(Obj("+", "-"))
    var ho2 = Holder(Obj("+", "-"))
    var ho3 = Holder(Obj("*", "*"))

    assertEquals(ho1, ho1)
    assertNotEquals(ho1, ho2)
    assertNotEquals(ho1, ho3)

    konst d1any: Any = d1
    assertTrue(d1any != "")

    return "OK"
}