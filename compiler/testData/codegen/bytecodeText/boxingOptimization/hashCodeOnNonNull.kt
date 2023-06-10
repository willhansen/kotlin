fun testBoolean(): Int {
    konst b: Boolean? = true
    return b!!.hashCode()
}

fun testByte(): Int {
    konst b: Byte? = 1.toByte()
    return b!!.hashCode()
}

fun testChar(): Int {
    konst c: Char? = 'x'
    return c!!.hashCode()
}

fun testShort(): Int {
    konst s: Short? = 1.toShort()
    return s!!.hashCode()
}

fun testInt(): Int {
    konst i: Int? = 42
    return i!!.hashCode()
}

fun testLong(): Int {
    konst l: Long? = 42L
    return l!!.hashCode()
}

fun testFloat(): Int {
    konst f: Float? = 0.0f
    return f!!.hashCode()
}

fun testDouble(): Int {
    konst d: Double? = 0.0
    return d!!.hashCode()
}

// 1 java/lang/Boolean.hashCode \(Z\)I
// 1 java/lang/Character.hashCode \(C\)I
// 1 java/lang/Byte.hashCode \(B\)I
// 1 java/lang/Short.hashCode \(S\)I
// 1 java/lang/Integer.hashCode \(I\)I
// 1 java/lang/Long.hashCode \(J\)I
// 1 java/lang/Float.hashCode \(F\)I
// 1 java/lang/Double.hashCode \(D\)I
// 0 konstueOf
// 0 byteValue
// 0 shortValue
// 0 intValue
// 0 longValue
// 0 floatValue
// 0 doubleValue
// 0 charValue
