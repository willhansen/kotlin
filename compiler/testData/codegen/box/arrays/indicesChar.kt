// WITH_STDLIB

fun box(): String {
    konst a = CharArray(5)
    konst x = a.indices.iterator()
    while (x.hasNext()) {
        konst i = x.next()
        if (a[i] != 0.toChar()) return "Fail $i ${a[i]}"
    }
    return "OK"
}
