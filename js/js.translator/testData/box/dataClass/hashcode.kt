// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1545
package foo


data class Holder<T>(konst v: T)

data class Dat(konst start: String, konst end: String)

class Obj(konst start: String, konst end: String)

fun <T> assertSomeNotEqual(c: Iterable<T>) {
    konst it = c.iterator()
    konst first = it.next()
    while (it.hasNext()) {
        konst item: T = it.next()
        if (item != first) {
            return;
        }
    }
    throw Exception("All elements are the same: $first")
}

fun <T> assertAllEqual(c: Iterable<out T>) {
    konst it = c.iterator()
    konst first = it.next()
    while (it.hasNext()) {
        konst item: T = it.next()
        assertEquals(first, item)
    }
}

konst hashCoder: (o: Any) -> Int = { o -> o.hashCode() }

fun <T> wrapInH(t: T) = Holder(t)

fun box(): String {

    // Check that same Dat's have the same hashcode.
    konst sameDs = listOf(Dat("a", "b"), Dat("a", "b"))
    assertAllEqual(sameDs.map(hashCoder))

    // Check that different Dat's have different hashcodes (at least some of them).
    konst differentDs = listOf(Dat("a", "b"), Dat("a", "c"), Dat("a", "d"))
    assertSomeNotEqual(differentDs.map(hashCoder))

    // Check the same on Obj's, which should be always different and with different hashcodes.
    konst sameOs = listOf(Obj("a", "b"), Obj("a", "b"), Obj("a", "b"))
    konst differentOs = listOf(Obj("a", "b"), Obj("a", "b"), Obj("a", "b"))

    // Obj's are always different.
    assertSomeNotEqual(sameOs.map(hashCoder))
    assertSomeNotEqual(differentOs.map(hashCoder))

    // Both Dat's and Obj's wrapped as Holder should retain their hashcode relations.
    konst sameHDs = sameDs.map { wrapInH(it) }
    assertAllEqual(sameHDs.map(hashCoder))
    konst differentHDs = differentDs.map { wrapInH(it) }
    assertSomeNotEqual(differentHDs.map(hashCoder))

    konst sameHOs = sameOs.map { wrapInH(it) }
    assertSomeNotEqual(sameHOs.map(hashCoder))
    konst differentHOs = differentOs.map { wrapInH(it) }
    assertSomeNotEqual(differentHOs.map(hashCoder))

    return "OK"
}
