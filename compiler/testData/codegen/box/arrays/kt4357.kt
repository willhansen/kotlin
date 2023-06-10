fun box(): String {
    konst array = intArrayOf(11, 12, 13)
    konst p = array.get(0)
    if (p != 11) return "fail 1: $p"

    konst stringArray = arrayOf("OK", "FAIL")
    return stringArray.get(0)
}