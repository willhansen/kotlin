// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst l = ArrayList<Int>()
    l.add(1)
    konst x = l[0] == 2
    if (x != false) return "Fail: $x}"
    return "OK"
}