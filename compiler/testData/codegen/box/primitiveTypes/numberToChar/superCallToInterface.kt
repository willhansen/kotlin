// DONT_TARGET_EXACT_BACKEND: JVM
// DONT_TARGET_EXACT_BACKEND: JS
// ISSUE: KT-46465
// WITH_STDLIB

interface Some {
    fun toChar(): Char = '+'
}

class MyNumber(konst konstue: Int) : Number(), Some {
    override fun toChar(): Char = super<Some>.toChar()
    override fun toInt(): Int = konstue

    override fun toByte(): Byte = toInt().toByte()
    override fun toDouble(): Double = toInt().toDouble()
    override fun toFloat(): Float = toInt().toFloat()
    override fun toLong(): Long = toInt().toLong()
    override fun toShort(): Short = toInt().toShort()
}

fun box(): String {
    konst x = MyNumber('*'.code).toChar()
    return if (x == '+') "OK" else "Fail: $x"
}
