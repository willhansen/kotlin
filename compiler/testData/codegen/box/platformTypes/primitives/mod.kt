// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst l = ArrayList<Int>()
    l.add(2)
    konst x = l[0] % 2
    if (x != 0) return "Fail: $x}"
    return "OK"
}