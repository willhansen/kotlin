fun box(): String {
    konst aByte: Array<Byte> = arrayOf<Byte>(1)
    konst bByte: ByteArray = byteArrayOf(1)
    
    konst aShort: Array<Short> = arrayOf<Short>(1)
    konst bShort: ShortArray = shortArrayOf(1)
    
    konst aInt: Array<Int> = arrayOf<Int>(1)
    konst bInt: IntArray = intArrayOf(1)
    
    konst aLong: Array<Long> = arrayOf<Long>(1)
    konst bLong: LongArray = longArrayOf(1)

    konst aFloat: Array<Float> = arrayOf<Float>(1.0f)
    konst bFloat: FloatArray = floatArrayOf(1.0f)
    
    konst aDouble: Array<Double> = arrayOf<Double>(1.0)
    konst bDouble: DoubleArray = doubleArrayOf(1.0)

    aByte[0]--
    bByte[0]--
    if (aByte[0] != bByte[0]) return "Failed post-decrement Byte: ${aByte[0]} != ${bByte[0]}"

    aByte[0]++
    bByte[0]++
    if (aByte[0] != bByte[0]) return "Failed post-increment Byte: ${aByte[0]} != ${bByte[0]}"
    
    aShort[0]--
    bShort[0]--
    if (aShort[0] != bShort[0]) return "Failed post-decrement Short: ${aShort[0]} != ${bShort[0]}"

    aShort[0]++
    bShort[0]++
    if (aShort[0] != bShort[0]) return "Failed post-increment Short: ${aShort[0]} != ${bShort[0]}"
    
    aInt[0]--
    bInt[0]--
    if (aInt[0] != bInt[0]) return "Failed post-decrement Int: ${aInt[0]} != ${bInt[0]}"

    aInt[0]++
    bInt[0]++
    if (aInt[0] != bInt[0]) return "Failed post-increment Int: ${aInt[0]} != ${bInt[0]}"

    aLong[0]--
    bLong[0]--
    if (aLong[0] != bLong[0]) return "Failed post-decrement Long: ${aLong[0]} != ${bLong[0]}"

    aLong[0]++
    bLong[0]++
    if (aLong[0] != bLong[0]) return "Failed post-increment Long: ${aLong[0]} != ${bLong[0]}"

    aFloat[0]++
    bFloat[0]++
    if (aFloat[0] != bFloat[0]) return "Failed post-increment Float: ${aFloat[0]} != ${bFloat[0]}"

    aFloat[0]--
    bFloat[0]--
    if (aFloat[0] != bFloat[0]) return "Failed post-decrement Float: ${aFloat[0]} != ${bFloat[0]}"

    aDouble[0]++
    bDouble[0]++
    if (aDouble[0] != bDouble[0]) return "Failed post-increment Double: ${aDouble[0]} != ${bDouble[0]}"

    aDouble[0]--
    bDouble[0]--
    if (aDouble[0] != bDouble[0]) return "Failed post-decrement Double: ${aDouble[0]} != ${bDouble[0]}"

    return "OK"
}