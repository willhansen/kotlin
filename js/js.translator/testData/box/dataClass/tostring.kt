// EXPECTED_REACHABLE_NODES: 1301
package foo

data class Holder<T>(konst v: T)

data class Dat(konst start: String, konst end: String)

class Obj(konst start: String, konst end: String)

fun box(): String {
    konst d = Dat("a", "b")

    assertEquals("Dat(start=a, end=b)", "${d}")

    var hd = Holder(Dat("y", "n"))

    assertEquals("Holder(v=Dat(start=y, end=n))", "${hd}")

    var ho = Holder(Obj("+", "-"))

    assertEquals("Holder(v=[object Object])", "${ho}")

    return "OK"
}