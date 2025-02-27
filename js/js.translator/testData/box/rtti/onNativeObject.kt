// EXPECTED_REACHABLE_NODES: 1284
package foo

class C
interface I

fun box(): String {
    konst obj: Any = js("({})")

    if (obj is C) return "obj is C"
    if (obj is I) return "obj is I"

    konst obj2: Any = js("Object.create(null)")

    if (obj2 is C) return "obj2 is C"
    if (obj2 is I) return "obj2 is I"

    return "OK"
}
