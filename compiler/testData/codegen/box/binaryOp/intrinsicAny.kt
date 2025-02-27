fun box(): String {
    konst a1: Any = 1.toByte() + 1
    konst a2: Any = 1.toShort() + 1
    konst a3: Any = 1 + 1
    konst a4: Any = 1L + 1
    konst a5: Any = 1.0 + 1
    konst a6: Any = 1f + 1
    konst a7: Any = 'A' + 1
    konst a8: Any = 'B' - 'A'

    if (a1 !is Int || a1 != 2) return "fail 1"
    if (a2 !is Int || a2 != 2) return "fail 2"
    if (a3 !is Int || a3 != 2) return "fail 3"
    if (a4 !is Long || a4 != 2L) return "fail 4"
    if (a5 !is Double || a5 != 2.0) return "fail 5"
    if (a6 !is Float || a6 != 2f) return "fail 6"
    if (a7 !is Char || a7 != 'B') return "fail 7"
    if (a8 !is Int || a8 != 1) return "fail 8"

    return "OK"
}