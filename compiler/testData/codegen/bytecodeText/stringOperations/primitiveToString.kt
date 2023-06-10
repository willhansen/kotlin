fun boolConst() = false.toString()
fun byteConst() = 1.toByte().toString()
fun shortConst() = 1.toShort().toString()
fun intConst() = 1.toString()
fun longConst() = 1L.toString()
fun floatConst() = 1.0F.toString()
fun doubleConst() = 1.0.toString()
fun charConst() = 'c'.toString()

/*Check that all "konstueOf" are String ones and there is no boxing*/
// JVM_TEMPLATES:
// 8 konstueOf
// 8 INVOKESTATIC java/lang/String.konstueOf
// JVM_IR_TEMPLATES:
// 0 konstueOf
// 8 LDC \"