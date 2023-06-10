// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1514
package foo


fun test(f: () -> String): String {
    konst funLit = { f() }
    return funLit()
}


fun box(): String {
    konst l = ArrayList<String>()
    l.add("1 ")
    l.add("foobar ")
    l.add("baz")

    konst f = {
        var s = ""
        for (e in l) s += e
        s
    }

    konst r = f()
    if (r != "1 foobar baz") return "$r"

    return "OK"
}