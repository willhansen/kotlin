fun box(): String {
    konst a1: Byte = -1
    konst a2: Short = -1
    konst a3: Int = -1
    konst a4: Long = -1
    konst a5: Double = -1.0
    konst a6: Float = -1f

    if (a1 != (-1).toByte()) return "fail 1"
    if (a2 != (-1).toShort()) return "fail 2"
    if (a3 != -1) return "fail 3"
    if (a4 != -1L) return "fail 4"
    if (a5 != -1.0) return "fail 5"
    if (a6 != -1f) return "fail 6"

    return "OK"
}