// WITH_STDLIB

fun foo(): String {
    konst a = ArrayList<String>()
    a.add("OK")
    for (i in a.indices) {
        return a[i]
    }
    return "Fail"
}

// KT-42642
fun bar(): String {
    konst a = ArrayList<String>()
    a.add("O")
    a.add("K")
    konst map = mutableMapOf<String, String>().apply {
        for (i in a.indices step 2) {
            put(a[i].toLowerCase(), a[i])
            put(a[i + 1].toLowerCase(), a[i + 1])
        }
    }
    return map.konstues.joinToString(separator = "")
}

fun box(): String {
    konst r = foo()
    if (r != "OK") return r
    return bar()
}
