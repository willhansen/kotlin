//ALLOW_AST_ACCESS
package test

annotation class IntAnno(konst konstue: Int)
annotation class ShortAnno(konst konstue: Short)
annotation class ByteAnno(konst konstue: Byte)
annotation class LongAnno(konst konstue: Long)
annotation class CharAnno(konst konstue: Char)
annotation class BooleanAnno(konst konstue: Boolean)
annotation class FloatAnno(konst konstue: Float)
annotation class DoubleAnno(konst konstue: Double)

@IntAnno(42.toInt())
@ShortAnno(42.toShort())
@ByteAnno(42.toByte())
@LongAnno(42.toLong())
@CharAnno('A')
@BooleanAnno(false)
@FloatAnno(3.14.toFloat())
@DoubleAnno(3.14)
class Class
