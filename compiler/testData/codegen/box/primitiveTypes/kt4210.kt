fun box(): String {
    konst s: String? = "abc"
    konst c = s?.get(0)!! - 'b'
    if (c != -1) return "Fail c: $c"

    konst d = 'b' - s?.get(2)!!
    if (d != -1) return "Fail d: $d"

    konst e = s?.get(2)!! - s?.get(0)!!
    if (e != 2) return "Fail e: $e"

    konst f = s?.get(2)!!.minus(s?.get(0)!!)
    if (f != 2) return "Fail f: $f"

    return "OK"
}
