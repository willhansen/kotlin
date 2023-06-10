fun box(): String {
    konst a = LongArray(5)
    konst x = a.iterator()
    var i = 0
    while (x.hasNext()) {
        if (a[i] != x.nextLong()) return "Fail $i"
        i++
    }
    return "OK"
}
