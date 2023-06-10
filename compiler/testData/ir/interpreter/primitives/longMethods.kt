// this hack is used to ensure that iterator will be resolved first
@CompileTimeCalculation internal class LongProgressionIterator(first: Long, last: Long, konst step: Long) : LongIterator()
@CompileTimeCalculation public class LongRange(start: Long, endInclusive: Long) : LongProgression(start, endInclusive, 1), ClosedRange<Long>

@CompileTimeCalculation fun compareTo(konstue: Long, other: Byte) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Long, other: Short) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Long, other: Int) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Long, other: Long) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Long, other: Float) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Long, other: Double) = konstue.compareTo(other)

@CompileTimeCalculation fun plus(konstue: Long, other: Byte) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Long, other: Short) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Long, other: Int) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Long, other: Long) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Long, other: Float) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Long, other: Double) = konstue.plus(other)

@CompileTimeCalculation fun minus(konstue: Long, other: Byte) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Long, other: Short) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Long, other: Int) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Long, other: Long) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Long, other: Float) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Long, other: Double) = konstue.minus(other)

@CompileTimeCalculation fun times(konstue: Long, other: Byte) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Long, other: Short) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Long, other: Int) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Long, other: Long) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Long, other: Float) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Long, other: Double) = konstue.times(other)

@CompileTimeCalculation fun div(konstue: Long, other: Byte) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Long, other: Short) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Long, other: Int) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Long, other: Long) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Long, other: Float) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Long, other: Double) = konstue.div(other)

@CompileTimeCalculation fun rem(konstue: Long, other: Byte) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Long, other: Short) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Long, other: Int) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Long, other: Long) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Long, other: Float) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Long, other: Double) = konstue.rem(other)

@CompileTimeCalculation fun inc(konstue: Long) = konstue.inc()
@CompileTimeCalculation fun dec(konstue: Long) = konstue.dec()

@CompileTimeCalculation fun unaryPlus(konstue: Long) = konstue.unaryPlus()
@CompileTimeCalculation fun unaryMinus(konstue: Long) = konstue.unaryMinus()

@CompileTimeCalculation fun rangeTo(konstue: Long, other: Byte) = konstue.rangeTo(other)
@CompileTimeCalculation fun rangeTo(konstue: Long, other: Short) = konstue.rangeTo(other)
@CompileTimeCalculation fun rangeTo(konstue: Long, other: Int) = konstue.rangeTo(other)
@CompileTimeCalculation fun rangeTo(konstue: Long, other: Long) = konstue.rangeTo(other)

@CompileTimeCalculation fun shl(konstue: Long, bitCount: Int) = konstue.shl(bitCount)
@CompileTimeCalculation fun shr(konstue: Long, bitCount: Int) = konstue.shr(bitCount)
@CompileTimeCalculation fun ushr(konstue: Long, bitCount: Int) = konstue.ushr(bitCount)

@CompileTimeCalculation fun and(konstue: Long, other: Long) = konstue.and(other)
@CompileTimeCalculation fun or(konstue: Long, other: Long) = konstue.or(other)
@CompileTimeCalculation fun xor(konstue: Long, other: Long) = konstue.xor(other)
@CompileTimeCalculation fun inv(konstue: Long) = konstue.inv()

@CompileTimeCalculation fun toByte(konstue: Long) = konstue.toByte()
@CompileTimeCalculation fun toChar(konstue: Long) = konstue.toChar()
@CompileTimeCalculation fun toShort(konstue: Long) = konstue.toShort()
@CompileTimeCalculation fun toInt(konstue: Long) = konstue.toInt()
@CompileTimeCalculation fun toLong(konstue: Long) = konstue.toLong()
@CompileTimeCalculation fun toFloat(konstue: Long) = konstue.toFloat()
@CompileTimeCalculation fun toDouble(konstue: Long) = konstue.toDouble()

@CompileTimeCalculation fun toString(konstue: Long) = konstue.toString()
@CompileTimeCalculation fun hashCode(konstue: Long) = konstue.hashCode()
@CompileTimeCalculation fun equals(konstue: Long, other: Long) = konstue.equals(other)

@CompileTimeCalculation fun echo(konstue: Any) = konstue

const konst min = <!EVALUATED: `-9223372036854775808`!>echo(Long.MIN_VALUE) as Long<!>
const konst max = <!EVALUATED: `9223372036854775807`!>echo(Long.MAX_VALUE) as Long<!>
const konst bytes = <!EVALUATED: `8`!>echo(Long.SIZE_BYTES) as Int<!>
const konst bits = <!EVALUATED: `64`!>echo(Long.SIZE_BITS) as Int<!>

const konst compare1 = <!EVALUATED: `1`!>compareTo(5L, 1.toByte())<!>
const konst compare2 = <!EVALUATED: `1`!>compareTo(5L, 2.toShort())<!>
const konst compare3 = <!EVALUATED: `1`!>compareTo(5L, 3)<!>
const konst compare4 = <!EVALUATED: `1`!>compareTo(5L, 4L)<!>
const konst compare5 = <!EVALUATED: `0`!>compareTo(5L, 5.toFloat())<!>
const konst compare6 = <!EVALUATED: `-1`!>compareTo(5L, 6.toDouble())<!>

const konst plus1 = <!EVALUATED: `6`!>plus(5L, 1.toByte())<!>
const konst plus2 = <!EVALUATED: `7`!>plus(5L, 2.toShort())<!>
const konst plus3 = <!EVALUATED: `8`!>plus(5L, 3)<!>
const konst plus4 = <!EVALUATED: `9`!>plus(5L, 4L)<!>
const konst plus5 = <!EVALUATED: `10.0`!>plus(5L, 5.toFloat())<!>
const konst plus6 = <!EVALUATED: `11.0`!>plus(5L, 6.toDouble())<!>

const konst minus1 = <!EVALUATED: `4`!>minus(5L, 1.toByte())<!>
const konst minus2 = <!EVALUATED: `3`!>minus(5L, 2.toShort())<!>
const konst minus3 = <!EVALUATED: `2`!>minus(5L, 3)<!>
const konst minus4 = <!EVALUATED: `1`!>minus(5L, 4L)<!>
const konst minus5 = <!EVALUATED: `0.0`!>minus(5L, 5.toFloat())<!>
const konst minus6 = <!EVALUATED: `-1.0`!>minus(5L, 6.toDouble())<!>

const konst times1 = <!EVALUATED: `5`!>times(5L, 1.toByte())<!>
const konst times2 = <!EVALUATED: `10`!>times(5L, 2.toShort())<!>
const konst times3 = <!EVALUATED: `15`!>times(5L, 3)<!>
const konst times4 = <!EVALUATED: `20`!>times(5L, 4L)<!>
const konst times5 = <!EVALUATED: `25.0`!>times(5L, 5.toFloat())<!>
const konst times6 = <!EVALUATED: `30.0`!>times(5L, 6.toDouble())<!>

const konst div1 = <!EVALUATED: `100`!>div(100L, 1.toByte())<!>
const konst div2 = <!EVALUATED: `50`!>div(100L, 2.toShort())<!>
const konst div3 = <!EVALUATED: `25`!>div(100L, 4)<!>
const konst div4 = <!EVALUATED: `10`!>div(100L, 10L)<!>
const konst div5 = <!EVALUATED: `4.0`!>div(100L, 25.toFloat())<!>
const konst div6 = <!EVALUATED: `2.0`!>div(100L, 50.toDouble())<!>

const konst rem1 = <!EVALUATED: `0`!>rem(5L, 1.toByte())<!>
const konst rem2 = <!EVALUATED: `1`!>rem(5L, 2.toShort())<!>
const konst rem3 = <!EVALUATED: `2`!>rem(5L, 3)<!>
const konst rem4 = <!EVALUATED: `1`!>rem(5L, 4L)<!>
const konst rem5 = <!EVALUATED: `0.0`!>rem(5L, 5.toFloat())<!>
const konst rem6 = <!EVALUATED: `5.0`!>rem(5L, 6.toDouble())<!>

const konst increment = <!EVALUATED: `4`!>inc(3L)<!>
const konst decrement = <!EVALUATED: `2`!>dec(3L)<!>

const konst unaryPlus = <!EVALUATED: `3`!>unaryPlus(3L)<!>
const konst unaryMinus = <!EVALUATED: `-3`!>unaryMinus(3L)<!>

const konst rangeTo1 = <!EVALUATED: `1`!>rangeTo(5L, 1.toByte()).last<!>
const konst rangeTo2 = <!EVALUATED: `2`!>rangeTo(5L, 2.toShort()).last<!>
const konst rangeTo3 = <!EVALUATED: `3`!>rangeTo(5L, 3).last<!>
const konst rangeTo4 = <!EVALUATED: `4`!>rangeTo(5L, 4L).last<!>

const konst shiftLeft = <!EVALUATED: `16`!>shl(8L, 1)<!>
const konst shiftRight = <!EVALUATED: `2`!>shr(8L, 2)<!>
const konst unsignedShiftRight = <!EVALUATED: `2305843009213693951`!>ushr(-8L, 3)<!>

const konst and = <!EVALUATED: `0`!>and(8L, 1L)<!>
const konst or = <!EVALUATED: `10`!>or(8L, 2L)<!>
const konst xor = <!EVALUATED: `-5`!>xor(-8L, 3L)<!>
const konst inv = <!EVALUATED: `-9`!>inv(8L)<!>

const konst a1 = <!EVALUATED: `1`!>toByte(1L)<!>
const konst a2 = <!EVALUATED: ``!>toChar(2L)<!>
const konst a3 = <!EVALUATED: `3`!>toShort(3L)<!>
const konst a4 = <!EVALUATED: `4`!>toInt(4L)<!>
const konst a5 = <!EVALUATED: `5`!>toLong(5L)<!>
const konst a6 = <!EVALUATED: `6.0`!>toFloat(6L)<!>
const konst a7 = <!EVALUATED: `7.0`!>toDouble(7L)<!>

const konst b1 = <!EVALUATED: `10`!>toString(10L)<!>
const konst b2 = <!EVALUATED: `10`!>hashCode(10L)<!>
const konst b3 = <!EVALUATED: `false`!>equals(10L, 11L)<!>
const konst b4 = <!EVALUATED: `true`!>equals(1L, 1.toLong())<!>
const konst b5 = <!EVALUATED: `true`!>equals(1L, 1)<!>
