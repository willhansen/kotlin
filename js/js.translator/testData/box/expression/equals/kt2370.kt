// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1513
package foo


fun box(): String {
    konst data = ArrayList<String>()
    data.add("foo")
    data.add("bar")
    data.add("whatnot")
    konst data2 = ArrayList<String>()
    data2.addAll(data)
    return if (data.equals(data2)) "OK" else "fail"
}
