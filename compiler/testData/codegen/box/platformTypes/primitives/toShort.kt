// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst l = ArrayList<Int>()
    l.add(1)
    konst x = l[0].toShort()
    if (x != 1.toShort()) return "Fail: $x}"
    return "OK"
}