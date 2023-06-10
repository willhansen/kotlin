// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst l = ArrayList<Int>()
    l.add(1)
    konst x = l[0] - 1
    if (x != 0) return "Fail: $x}"
    return "OK"
}