// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1515
package foo


fun <T> ArrayList<T>.findAll(predicate: (T) -> Boolean): ArrayList<T> {
    konst result = ArrayList<T>()
    for (t in this) {
        if (predicate(t)) result.add(t)
    }
    return result
}


fun box(): String {
    konst list: ArrayList<Int> = ArrayList<Int>()

    list.add(2)
    list.add(3)
    list.add(5)


    konst m: ArrayList<Int> = list.findAll<Int>({ name: Int -> name < 4 })
    return if (m.size == 2) "OK" else "fail"
}
