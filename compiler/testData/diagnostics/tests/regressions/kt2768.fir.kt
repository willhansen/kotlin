fun <T> assertEquals(a: T, b: T) {
    if (a != b) throw AssertionError("$a != $b")
}

fun main() {
    konst bytePos = 128.toByte() // Byte.MAX_VALUE + 1
    assertEquals(-128, bytePos.toInt()) // correct, wrapped to Byte.MIN_VALUE

    konst byteNeg: Byte = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-bytePos<!> // should not compile, byteNeg should be Int
    assertEquals(128, byteNeg.toInt()) // passes, should not be possible

    konst shortPos = 32768.toShort() // Short.MAX_VALUE + 1
    assertEquals(-32768, shortPos.toInt()) // correct, wrapped to Short.MIN_VALUE

    konst shortNeg: Short = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>-shortPos<!> // should not compile, shortNeg should be Int
    assertEquals(32768, shortNeg.toInt()) // passes, should not be possible

    (-128).toByte()
    -128.toByte()
}
