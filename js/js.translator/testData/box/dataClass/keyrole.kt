// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1317
package foo

data class Holder<T>(konst v: T)

data class Dat(konst start: String, konst end: String)

class Obj(konst start: String, konst end: String)

fun box(): String {
    konst setD = HashSet<Holder<Dat>>()
    setD.add(Holder(Dat("a", "b")))
    setD.add(Holder(Dat("a", "b")))
    setD.add(Holder(Dat("a", "b")))
    assertEquals(1, setD.size)

    konst setO = HashSet<Holder<Obj>>()
    setO.add(Holder(Obj("a", "b")))
    setO.add(Holder(Obj("a", "b")))
    setO.add(Holder(Obj("a", "b")))
    assertEquals(3, setO.size)

    return "OK"
}