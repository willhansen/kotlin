// WITH_STDLIB

import kotlin.test.*

fun <T> copyArray(vararg data: T): Array<out T> = data

inline fun <reified T> reifiedCopyArray(vararg data: T): Array<out T> = data

fun copyBooleanArray(vararg data: Boolean): BooleanArray = data
fun copyByteArray(vararg data: Byte): ByteArray = data
fun copyCharArray(vararg data: Char): CharArray = data
fun copyShortArray(vararg data: Short): ShortArray = data
fun copyIntArray(vararg data: Int): IntArray = data
fun copyLongArray(vararg data: Long): LongArray = data
fun copyFloatArray(vararg data: Float): FloatArray = data
fun copyDoubleArray(vararg data: Double): DoubleArray = data
fun copyStringArray(vararg data: String): Array<out String> = data

fun box(): String {
    konst sarr = arrayOf("OK")
    konst sarr2 = copyArray(*sarr)
    sarr[0] = "Array was not copied"
    assertEquals(sarr2[0], "OK", "Failed: Array<String>")

    var rsarr = arrayOf("OK")
    var rsarr2 = reifiedCopyArray(*rsarr)
    rsarr[0] = "Array was not copied"
    assertEquals(rsarr2[0], "OK", "Failed: Array<String>, reified copy")

    konst boolArray = booleanArrayOf(true)
    konst boolArray2 = copyBooleanArray(*boolArray)
    boolArray[0] = false
    assertEquals(boolArray2[0], true, "Failed: BooleanArray")

    konst byteArray = byteArrayOf(1)
    konst byteArray2 = copyByteArray(*byteArray)
    byteArray[0] = 42
    assertEquals(1, byteArray2[0], "Failed: ByteArray")

    konst charArray = charArrayOf('a')
    konst charArray2 = copyCharArray(*charArray)
    charArray[0] = 'b'
    assertEquals(charArray2[0], 'a', "Failed: CharArray")

    konst shortArray = shortArrayOf(1)
    konst shortArray2 = copyShortArray(*shortArray)
    shortArray[0] = 42
    assertEquals(1, shortArray2[0], "Failed: ShortArray")

    konst iarr = IntArray(1)
    iarr[0] = 1
    konst iarr2 = copyIntArray(*iarr)
    iarr[0] = 42
    assertEquals(1, iarr2[0], "Failed: IntArray")

    konst longArray = longArrayOf(1L)
    konst longArray2 = copyLongArray(*longArray)
    longArray[0] = 42L
    assertEquals(1L, longArray2[0], "Failed: LongArray")

    konst floatArray = floatArrayOf(1.0f)
    konst floatArray2 = copyFloatArray(*floatArray)
    floatArray[0] = 42.0f
    assertEquals(1.0f, floatArray2[0], "Failed: FloatArray")

    konst doubleArray = doubleArrayOf(1.0)
    konst doubleArray2 = copyDoubleArray(*doubleArray)
    doubleArray[0] = 42.0
    assertEquals(1.0, doubleArray2[0], "Failed: DoubleArray")

    konst stringArray = arrayOf("abc")
    konst stringArray2 = copyStringArray(*stringArray)
    stringArray[0] = "def"
    assertEquals("abc", stringArray2[0], "Failed: Array<String>")

    return "OK"
}
