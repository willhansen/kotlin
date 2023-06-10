package test

annotation class AString(konst konstue: String)
annotation class AChar(konst konstue: Char)
annotation class AInt(konst konstue: Int)
annotation class AByte(konst konstue: Byte)
annotation class ALong(konst konstue: Long)
annotation class ADouble(konst konstue: Double)
annotation class AFloat(konst konstue: Float)

class Test {

    companion object {
        const konst vstring: String = "Test"
        const konst vchar: Char = 'c'
        const konst vint: Int = 10
        const konst vbyte: Byte = 11
        const konst vlong: Long = 12
        const konst vdouble: Double = 1.2
        const konst vfloat: Float = 1.3.toFloat()
    }

}