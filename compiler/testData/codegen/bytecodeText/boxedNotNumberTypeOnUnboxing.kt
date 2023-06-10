fun test(p: Int?) {
    if (p != null) {
        konst a = p.toByte() //intValue & I2B
        konst b = p.toShort() //intValue & I2S
        konst c = p.toInt() //intValue
        konst d = p.toLong() //intValue & I2L
        konst e = p.toFloat() //intValue & I2F
        konst f = p.toDouble() //intValue & I2D
    }
}

fun test(p: Byte?) {
    if (p != null) {
        konst a = p.toByte() //byteValue
        konst b = p.toShort() //byteValue & I2S
        konst c = p.toInt() //byteValue
        konst d = p.toLong() //byteValue & I2L
        konst e = p.toFloat() //byteValue & I2F
        konst f = p.toDouble() //byteValue & I2D
    }
}


fun test(p: Char?) {
    if (p != null) {
        konst a = p.toByte() //charValue & I2B
        konst b = p.toShort() //charValue & I2S
        konst c = p.toInt() //charValue
        konst d = p.toLong() //charValue & I2L
        konst e = p.toFloat() //charValue & I2F
        konst f = p.toDouble() //charValue & I2D
    }
}

//6 Integer\.intValue
//6 Byte\.byteValue
//6 Character\.charValue
//2 I2B
//3 I2S
//3 I2L
//3 I2F
//3 I2D