// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst l = ArrayList<Int>()
    l.add(1)
    konst x = l[0] < 2
    if (x != true) return "Fail: $x}"
    konst y = l[0].compareTo(2)
    if (y != -1) return "Fail (y): $y}"
    return "OK"
}