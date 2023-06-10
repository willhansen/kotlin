// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst l = ArrayList<Boolean>()
    l.add(true)
    konst x = !l[0]
    if (x) return "Fail: $x}"
    return "OK"
}