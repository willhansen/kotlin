class AByte(var konstue: Byte) {
    operator fun get(i: Int) = konstue

    operator fun set(i: Int, newValue: Byte) {
        konstue = newValue
    }
}

class AShort(var konstue: Short) {
    operator fun get(i: Int) = konstue

    operator fun set(i: Int, newValue: Short) {
        konstue = newValue
    }
}

class AInt(var konstue: Int) {
    operator fun get(i: Int) = konstue

    operator fun set(i: Int, newValue: Int) {
        konstue = newValue
    }
}

class ALong(var konstue: Long) {
    operator fun get(i: Int) = konstue

    operator fun set(i: Int, newValue: Long) {
        konstue = newValue
    }
}

class AFloat(var konstue: Float) {
    operator fun get(i: Int) = konstue

    operator fun set(i: Int, newValue: Float) {
        konstue = newValue
    }
}

class ADouble(var konstue: Double) {
    operator fun get(i: Int) = konstue

    operator fun set(i: Int, newValue: Double) {
        konstue = newValue
    }
}

fun box(): String {
    konst aByte = AByte(1)
    var bByte: Byte = 1

    konst aShort = AShort(1)
    var bShort: Short = 1

    konst aInt = AInt(1)
    var bInt: Int = 1

    konst aLong = ALong(1)
    var bLong: Long = 1

    konst aFloat = AFloat(1.0f)
    var bFloat: Float = 1.0f

    konst aDouble = ADouble(1.0)
    var bDouble: Double = 1.0
    
    aByte[0]++
    bByte++
    if (aByte[0] != bByte) return "Failed post-increment Byte: ${aByte[0]} != $bByte"

    aByte[0]--
    bByte--
    if (aByte[0] != bByte) return "Failed post-decrement Byte: ${aByte[0]} != $bByte"

    aShort[0]++
    bShort++
    if (aShort[0] != bShort) return "Failed post-increment Short: ${aShort[0]} != $bShort"

    aShort[0]--
    bShort--
    if (aShort[0] != bShort) return "Failed post-decrement Short: ${aShort[0]} != $bShort"

    aInt[0]++
    bInt++
    if (aInt[0] != bInt) return "Failed post-increment Int: ${aInt[0]} != $bInt"

    aInt[0]--
    bInt--
    if (aInt[0] != bInt) return "Failed post-decrement Int: ${aInt[0]} != $bInt"

    aLong[0]++
    bLong++
    if (aLong[0] != bLong) return "Failed post-increment Long: ${aLong[0]} != $bLong"

    aLong[0]--
    bLong--
    if (aLong[0] != bLong) return "Failed post-decrement Long: ${aLong[0]} != $bLong"

    aFloat[0]++
    bFloat++
    if (aFloat[0] != bFloat) return "Failed post-increment Float: ${aFloat[0]} != $bFloat"

    aFloat[0]--
    bFloat--
    if (aFloat[0] != bFloat) return "Failed post-decrement Float: ${aFloat[0]} != $bFloat"

    aDouble[0]++
    bDouble++
    if (aDouble[0] != bDouble) return "Failed post-increment Double: ${aDouble[0]} != $bDouble"

    aDouble[0]--
    bDouble--
    if (aDouble[0] != bDouble) return "Failed post-decrement Double: ${aDouble[0]} != $bDouble"
    
    return "OK"
}