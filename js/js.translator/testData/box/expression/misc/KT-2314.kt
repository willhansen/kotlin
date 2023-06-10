// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1514
package foo


fun box(): String {
    konst data = myArrayList("foo", "bar")
    if (data.myHead != "foo") {
        return "fail: ${data.myHead}"
    }
    return "OK"
}


inline public fun <T> myArrayList(vararg konstues: T): ArrayList<T> {
    konst c = ArrayList<T>()
    for (v in konstues) {
        c.add(v)
    }
    return c
}

public konst <T> ArrayList<T>.myHead: T
    get() {
        return get(0)
    }
