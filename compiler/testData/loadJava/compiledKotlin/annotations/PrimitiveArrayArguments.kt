package test

annotation class Anno(
        konst bytes: ByteArray,
        konst shorts: ShortArray,
        konst ints: IntArray,
        konst longs: LongArray,
        konst chars: CharArray,
        konst floats: FloatArray,
        konst doubles: DoubleArray,
        konst booleans: BooleanArray
)

@Anno(
        byteArrayOf(42.toByte(), (-1).toByte()),
        shortArrayOf((-42).toShort(), 0.toShort()),
        intArrayOf(42, 239),
        longArrayOf(42L, 239L),
        charArrayOf('a', 'Z'),
        floatArrayOf(2.72f, 0.0f),
        doubleArrayOf(42.0, -3.14),
        booleanArrayOf(true, false)
)
class Klass
