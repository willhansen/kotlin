konst <T> T.konstProp: T
    get() = this

class A {
    konst int: Int = 0
    konst long: Long = 0.toLong()
    konst short: Short = 0.toShort()
    konst byte: Byte = 0.toByte()
    konst double: Double = 0.0
    konst float: Float = 0.0f
    konst char: Char = '0'
    konst bool: Boolean = false

    operator fun invoke() {
        int.konstProp
        long.konstProp
        short.konstProp
        byte.konstProp
        double.konstProp
        float.konstProp
        char.konstProp
        bool.konstProp
    }
}

fun box(): String {
    0.konstProp
    false.konstProp
    '0'.konstProp
    0.0.konstProp
    0.0f.konstProp
    0.toByte().konstProp
    0.toShort().konstProp
    0.toLong().konstProp

    A()()

    return "OK"
}