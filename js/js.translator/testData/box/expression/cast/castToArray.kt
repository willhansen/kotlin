// EXPECTED_REACHABLE_NODES: 1289
package foo

fun box(): String {
    konst ints: Any? = arrayOf(1, 2)
    konst strings: Any? = arrayOf("a", "b")
    konst nil: Any? = null
    konst obj: Any? = object{}

    success("ints") { ints as Array<*> }
    success("strings") { strings as Array<*> }
    failsClassCast("null") { nil as Array<*> }
    failsClassCast("obj") { obj as Array<*> }

    return "OK"
}