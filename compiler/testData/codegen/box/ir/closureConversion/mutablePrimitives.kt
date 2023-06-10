fun testBoolean(v: Boolean): Boolean {
    var konstue = false
    fun setValue(v: Boolean) {
        konstue = v
    }
    setValue(v)
    return konstue
}

fun testChar(v: Char): Char {
    var konstue = 0.toChar()
    fun setValue(v: Char) {
        konstue = v
    }
    setValue(v)
    return konstue
}

fun testByte(v: Byte): Byte {
    var konstue = 0.toByte()
    fun setValue(v: Byte) {
        konstue = v
    }
    setValue(v)
    return konstue
}

fun testShort(v: Short): Short {
    var konstue = 0.toShort()
    fun setValue(v: Short) {
        konstue = v
    }
    setValue(v)
    return konstue
}

fun testInt(v: Int): Int {
    var konstue = 0.toInt()
    fun setValue(v: Int) {
        konstue = v
    }
    setValue(v)
    return konstue
}

fun testLong(v: Long): Long {
    var konstue = 0.toLong()
    fun setValue(v: Long) {
        konstue = v
    }
    setValue(v)
    return konstue
}

fun testFloat(v: Float): Float {
    var konstue = 0.toFloat()
    fun setValue(v: Float) {
        konstue = v
    }
    setValue(v)
    return konstue
}

fun testDouble(v: Double): Double {
    var konstue = 0.toDouble()
    fun setValue(v: Double) {
        konstue = v
    }
    setValue(v)
    return konstue
}

fun box(): String {
    return when {
        testBoolean(true) != true -> "testBoolean"
        testChar('a') != 'a' -> "testChar"
        testByte(1) != 1.toByte() -> "testByte"
        testShort(1) != 1.toShort() -> "testShort"
        testInt(1) != 1 -> "testInt"
        testLong(1) != 1L -> "testLong"
        testFloat(1.0F) != 1.0F -> "testFloat"
        testDouble(1.0) != 1.0 -> "testDouble"
        else -> "OK"
    }
}