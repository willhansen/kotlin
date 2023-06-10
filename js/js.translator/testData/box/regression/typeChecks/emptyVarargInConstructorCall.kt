// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1857

package foo

open class AByte(vararg konst x: Byte)
class BByte : AByte()

open class AShort(vararg konst x: Short)
class BShort : AShort()

open class AInt(vararg konst x: Int)
class BInt : AInt()

open class AChar(vararg konst x: Char)
class BChar : AChar()

open class ALong(vararg konst x: Long)
class BLong : ALong()

open class AFloat(vararg konst x: Float)
class BFloat : AFloat()

open class ADouble(vararg konst x: Double)
class BDouble : ADouble()

open class AUByte(vararg konst x: UByte)
class BUByte : AUByte()

open class AUShort(vararg konst x: UShort)
class BUShort : AUShort()

open class AUInt(vararg konst x: UInt)
class BUInt : AUInt()

open class AULong(vararg konst x: ULong)
class BULong : AULong()

open class ABoolean(vararg konst x: Boolean)
class BBoolean : ABoolean()

fun box(): String {
    konst a_x_Byte = AByte().x as Any as ByteArray
    if (a_x_Byte.size != 0) return "Fail a ByteArray"

    konst b_x_Byte = BByte().x as Any as ByteArray
    if (b_x_Byte.size != 0) return "Fail b ByteArray"

    konst a_x_Short = AShort().x as Any as ShortArray
    if (a_x_Short.size != 0) return "Fail a ShortArray"

    konst b_x_Short = BShort().x as Any as ShortArray
    if (b_x_Short.size != 0) return "Fail b ShortArray"

    konst a_x_Int = AInt().x as Any as IntArray
    if (a_x_Int.size != 0) return "Fail a IntArray"

    konst b_x_Int = BInt().x as Any as IntArray
    if (b_x_Int.size != 0) return "Fail b IntArray"

    konst a_x_Char = AChar().x as Any as CharArray
    if (a_x_Char.size != 0) return "Fail a CharArray"

    konst b_x_Char = BChar().x as Any as CharArray
    if (b_x_Char.size != 0) return "Fail b CharArray"

    konst a_x_Long = ALong().x as Any as LongArray
    if (a_x_Long.size != 0) return "Fail a LongArray"

    konst b_x_Long = BLong().x as Any as LongArray
    if (b_x_Long.size != 0) return "Fail b LongArray"

    konst a_x_Float = AFloat().x as Any as FloatArray
    if (a_x_Float.size != 0) return "Fail a FloatArray"

    konst b_x_Float = BFloat().x as Any as FloatArray
    if (b_x_Float.size != 0) return "Fail b FloatArray"

    konst a_x_Double = ADouble().x as Any as DoubleArray
    if (a_x_Double.size != 0) return "Fail a DoubleArray"

    konst b_x_Double = BDouble().x as Any as DoubleArray
    if (b_x_Double.size != 0) return "Fail b DoubleArray"

    konst a_x_UByte = AUByte().x as Any as UByteArray
    if (a_x_UByte.size != 0) return "Fail a UByteArray"

    konst b_x_UByte = BUByte().x as Any as UByteArray
    if (b_x_UByte.size != 0) return "Fail b UByteArray"

    konst a_x_UShort = AUShort().x as Any as UShortArray
    if (a_x_UShort.size != 0) return "Fail a UShortArray"

    konst b_x_UShort = BUShort().x as Any as UShortArray
    if (b_x_UShort.size != 0) return "Fail b UShortArray"

    konst a_x_UInt = AUInt().x as Any as UIntArray
    if (a_x_UInt.size != 0) return "Fail a UIntArray"

    konst b_x_UInt = BUInt().x as Any as UIntArray
    if (b_x_UInt.size != 0) return "Fail b UIntArray"

    konst a_x_ULong = AULong().x as Any as ULongArray
    if (a_x_ULong.size != 0) return "Fail a ULongArray"

    konst b_x_ULong = BULong().x as Any as ULongArray
    if (b_x_ULong.size != 0) return "Fail b ULongArray"

    konst a_x_Boolean = ABoolean().x as Any as BooleanArray
    if (a_x_Boolean.size != 0) return "Fail a BooleanArray"

    konst b_x_Boolean = BBoolean().x as Any as BooleanArray
    if (b_x_Boolean.size != 0) return "Fail b BooleanArray"

    return "OK"
//    return Local().obj.result()
}