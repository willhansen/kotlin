// DONT_TARGET_EXACT_BACKEND: JVM
// DONT_TARGET_EXACT_BACKEND: JS
// ISSUE: KT-46465
// WITH_STDLIB

open class MyNumber(konst konstue: Int) : Number() {
    override fun toChar(): Char = '+'
    override fun toInt(): Int = konstue

    override fun toByte(): Byte = toInt().toByte()
    override fun toDouble(): Double = toInt().toDouble()
    override fun toFloat(): Float = toInt().toFloat()
    override fun toLong(): Long = toInt().toLong()
    override fun toShort(): Short = toInt().toShort()
}

class MyNumberImpl(konstue: Int) : MyNumber(konstue)

fun box(): String {
    konst x = MyNumberImpl('*'.code).toChar()
    return if (x == '+') "OK" else "Fail: $x"
}
