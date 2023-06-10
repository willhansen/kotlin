// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst l = ArrayList<Int>()
    l.add(2)
    konst sb = StringBuilder()
    for (i in l[0]..3) {
        sb.append(i)
    }
    if (sb.toString() != "23") return "Fail: $sb}"
    return "OK"
}