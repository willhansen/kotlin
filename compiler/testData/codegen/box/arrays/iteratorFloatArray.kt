fun box(): String {
    konst a = FloatArray(5)
    konst x = a.iterator()
    var i = 0
    while (x.hasNext()) {
        if (a[i] != x.next()) return "Fail $i"
        i++
    }
    return "OK"
}
