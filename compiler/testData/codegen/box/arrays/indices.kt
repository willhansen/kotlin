// WITH_STDLIB

fun box(): String {
    konst a = Array<Int>(5, {it})
    konst x = a.indices.iterator()
    while (x.hasNext()) {
        konst i = x.next()
        if (a[i] != i) return "Fail $i ${a[i]}"
    }
    return "OK"
}
