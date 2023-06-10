package test

annotation class PrimitiveArrays(
        konst byteArray: ByteArray,
        konst charArray: CharArray,
        konst shortArray: ShortArray,
        konst intArray: IntArray,
        konst longArray: LongArray,
        konst floatArray: FloatArray,
        konst doubleArray: DoubleArray,
        konst booleanArray: BooleanArray
)

@PrimitiveArrays(
        byteArray = byteArrayOf(-7, 7),
        charArray = charArrayOf('%', 'z'),
        shortArray = shortArrayOf(239),
        intArray = intArrayOf(239017, -1),
        longArray = longArrayOf(123456789123456789L),
        floatArray = floatArrayOf(2.72f, 0f),
        doubleArray = doubleArrayOf(-3.14),
        booleanArray = booleanArrayOf(true, false, true)
)
class C1

@PrimitiveArrays(
        byteArray = byteArrayOf(),
        charArray = charArrayOf(),
        shortArray = shortArrayOf(),
        intArray = intArrayOf(),
        longArray = longArrayOf(),
        floatArray = floatArrayOf(),
        doubleArray = doubleArrayOf(),
        booleanArray = booleanArrayOf()
)
class C2
