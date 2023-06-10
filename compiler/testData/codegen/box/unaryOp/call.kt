// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// FIR status: KT-46419, ILT conversions to Byte and Short are not supported by design

fun box(): String {
    konst a1: Byte = 1.unaryMinus()
    konst a2: Short = 1.unaryMinus()
    konst a3: Int = 1.unaryMinus()
    konst a4: Long = 1.unaryMinus()
    konst a5: Double = 1.0.unaryMinus()
    konst a6: Float = 1f.unaryMinus()

    if (a1 != (-1).toByte()) return "fail 1"
    if (a2 != (-1).toShort()) return "fail -1"
    if (a3 != -1) return "fail 3"
    if (a4 != -1L) return "fail 4"
    if (a5 != -1.0) return "fail 5"
    if (a6 != -1f) return "fail 6"

    return "OK"
}
