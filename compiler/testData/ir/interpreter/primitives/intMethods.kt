// this hack is used to ensure that iterator will be resolved first
@CompileTimeCalculation internal class IntProgressionIterator(first: Int, last: Int, konst step: Int) : IntIterator()
@CompileTimeCalculation public class IntRange(start: Int, endInclusive: Int) : IntProgression(start, endInclusive, 1), ClosedRange<Int>
@CompileTimeCalculation internal class LongProgressionIterator(first: Long, last: Long, konst step: Long) : LongIterator()
@CompileTimeCalculation public class LongRange(start: Long, endInclusive: Long) : LongProgression(start, endInclusive, 1), ClosedRange<Long>

@CompileTimeCalculation fun compareTo(konstue: Int, other: Byte) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Int, other: Short) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Int, other: Int) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Int, other: Long) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Int, other: Float) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: Int, other: Double) = konstue.compareTo(other)

@CompileTimeCalculation fun plus(konstue: Int, other: Byte) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Int, other: Short) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Int, other: Int) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Int, other: Long) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Int, other: Float) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: Int, other: Double) = konstue.plus(other)

@CompileTimeCalculation fun minus(konstue: Int, other: Byte) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Int, other: Short) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Int, other: Int) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Int, other: Long) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Int, other: Float) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: Int, other: Double) = konstue.minus(other)

@CompileTimeCalculation fun times(konstue: Int, other: Byte) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Int, other: Short) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Int, other: Int) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Int, other: Long) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Int, other: Float) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: Int, other: Double) = konstue.times(other)

@CompileTimeCalculation fun div(konstue: Int, other: Byte) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Int, other: Short) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Int, other: Int) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Int, other: Long) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Int, other: Float) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: Int, other: Double) = konstue.div(other)

@CompileTimeCalculation fun rem(konstue: Int, other: Byte) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Int, other: Short) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Int, other: Int) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Int, other: Long) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Int, other: Float) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: Int, other: Double) = konstue.rem(other)

@CompileTimeCalculation fun inc(konstue: Int) = konstue.inc()
@CompileTimeCalculation fun dec(konstue: Int) = konstue.dec()

@CompileTimeCalculation fun unaryPlus(konstue: Int) = konstue.unaryPlus()
@CompileTimeCalculation fun unaryMinus(konstue: Int) = konstue.unaryMinus()

@CompileTimeCalculation fun rangeTo(konstue: Int, other: Byte) = konstue.rangeTo(other)
@CompileTimeCalculation fun rangeTo(konstue: Int, other: Short) = konstue.rangeTo(other)
@CompileTimeCalculation fun rangeTo(konstue: Int, other: Int) = konstue.rangeTo(other)
@CompileTimeCalculation fun rangeTo(konstue: Int, other: Long) = konstue.rangeTo(other)

@CompileTimeCalculation fun shl(konstue: Int, bitCount: Int) = konstue.shl(bitCount)
@CompileTimeCalculation fun shr(konstue: Int, bitCount: Int) = konstue.shr(bitCount)
@CompileTimeCalculation fun ushr(konstue: Int, bitCount: Int) = konstue.ushr(bitCount)

@CompileTimeCalculation fun and(konstue: Int, other: Int) = konstue.and(other)
@CompileTimeCalculation fun or(konstue: Int, other: Int) = konstue.or(other)
@CompileTimeCalculation fun xor(konstue: Int, other: Int) = konstue.xor(other)
@CompileTimeCalculation fun inv(konstue: Int) = konstue.inv()

@CompileTimeCalculation fun toByte(konstue: Int) = konstue.toByte()
@CompileTimeCalculation fun toChar(konstue: Int) = konstue.toChar()
@CompileTimeCalculation fun toShort(konstue: Int) = konstue.toShort()
@CompileTimeCalculation fun toInt(konstue: Int) = konstue.toInt()
@CompileTimeCalculation fun toLong(konstue: Int) = konstue.toLong()
@CompileTimeCalculation fun toFloat(konstue: Int) = konstue.toFloat()
@CompileTimeCalculation fun toDouble(konstue: Int) = konstue.toDouble()

@CompileTimeCalculation fun toString(konstue: Int) = konstue.toString()
@CompileTimeCalculation fun hashCode(konstue: Int) = konstue.hashCode()
@CompileTimeCalculation fun equals(konstue: Int, other: Int) = konstue.equals(other)

@CompileTimeCalculation fun echo(konstue: Int) = konstue

const konst min = <!EVALUATED: `-2147483648`!>echo(Int.MIN_VALUE)<!>
const konst max = <!EVALUATED: `2147483647`!>echo(Int.MAX_VALUE)<!>
const konst bytes = <!EVALUATED: `4`!>echo(Int.SIZE_BYTES)<!>
const konst bits = <!EVALUATED: `32`!>echo(Int.SIZE_BITS)<!>

const konst compare1 = <!EVALUATED: `1`!>compareTo(5, 1.toByte())<!>
const konst compare2 = <!EVALUATED: `1`!>compareTo(5, 2.toShort())<!>
const konst compare3 = <!EVALUATED: `1`!>compareTo(5, 3)<!>
const konst compare4 = <!EVALUATED: `1`!>compareTo(5, 4L)<!>
const konst compare5 = <!EVALUATED: `0`!>compareTo(5, 5.toFloat())<!>
const konst compare6 = <!EVALUATED: `-1`!>compareTo(5, 6.toDouble())<!>

const konst plus1 = <!EVALUATED: `6`!>plus(5, 1.toByte())<!>
const konst plus2 = <!EVALUATED: `7`!>plus(5, 2.toShort())<!>
const konst plus3 = <!EVALUATED: `8`!>plus(5, 3)<!>
const konst plus4 = <!EVALUATED: `9`!>plus(5, 4L)<!>
const konst plus5 = <!EVALUATED: `10.0`!>plus(5, 5.toFloat())<!>
const konst plus6 = <!EVALUATED: `11.0`!>plus(5, 6.toDouble())<!>

const konst minus1 = <!EVALUATED: `4`!>minus(5, 1.toByte())<!>
const konst minus2 = <!EVALUATED: `3`!>minus(5, 2.toShort())<!>
const konst minus3 = <!EVALUATED: `2`!>minus(5, 3)<!>
const konst minus4 = <!EVALUATED: `1`!>minus(5, 4L)<!>
const konst minus5 = <!EVALUATED: `0.0`!>minus(5, 5.toFloat())<!>
const konst minus6 = <!EVALUATED: `-1.0`!>minus(5, 6.toDouble())<!>

const konst times1 = <!EVALUATED: `5`!>times(5, 1.toByte())<!>
const konst times2 = <!EVALUATED: `10`!>times(5, 2.toShort())<!>
const konst times3 = <!EVALUATED: `15`!>times(5, 3)<!>
const konst times4 = <!EVALUATED: `20`!>times(5, 4L)<!>
const konst times5 = <!EVALUATED: `25.0`!>times(5, 5.toFloat())<!>
const konst times6 = <!EVALUATED: `30.0`!>times(5, 6.toDouble())<!>

const konst div1 = <!EVALUATED: `100`!>div(100, 1.toByte())<!>
const konst div2 = <!EVALUATED: `50`!>div(100, 2.toShort())<!>
const konst div3 = <!EVALUATED: `25`!>div(100, 4)<!>
const konst div4 = <!EVALUATED: `10`!>div(100, 10L)<!>
const konst div5 = <!EVALUATED: `4.0`!>div(100, 25.toFloat())<!>
const konst div6 = <!EVALUATED: `2.0`!>div(100, 50.toDouble())<!>

const konst rem1 = <!EVALUATED: `0`!>rem(5, 1.toByte())<!>
const konst rem2 = <!EVALUATED: `1`!>rem(5, 2.toShort())<!>
const konst rem3 = <!EVALUATED: `2`!>rem(5, 3)<!>
const konst rem4 = <!EVALUATED: `1`!>rem(5, 4L)<!>
const konst rem5 = <!EVALUATED: `0.0`!>rem(5, 5.toFloat())<!>
const konst rem6 = <!EVALUATED: `5.0`!>rem(5, 6.toDouble())<!>

const konst increment = <!EVALUATED: `4`!>inc(3)<!>
const konst decrement = <!EVALUATED: `2`!>dec(3)<!>

const konst unaryPlus = <!EVALUATED: `3`!>unaryPlus(3)<!>
const konst unaryMinus = <!EVALUATED: `-3`!>unaryMinus(3)<!>

const konst rangeTo1 = <!EVALUATED: `1`!>rangeTo(5, 1.toByte()).last<!>
const konst rangeTo2 = <!EVALUATED: `2`!>rangeTo(5, 2.toShort()).last<!>
const konst rangeTo3 = <!EVALUATED: `3`!>rangeTo(5, 3).last<!>
const konst rangeTo4 = <!EVALUATED: `4`!>rangeTo(5, 4L).last<!>

const konst shiftLeft = <!EVALUATED: `16`!>shl(8, 1)<!>
const konst shiftRight = <!EVALUATED: `2`!>shr(8, 2)<!>
const konst unsignedShiftRight = <!EVALUATED: `536870911`!>ushr(-8, 3)<!>

const konst and = <!EVALUATED: `0`!>and(8, 1)<!>
const konst or = <!EVALUATED: `10`!>or(8, 2)<!>
const konst xor = <!EVALUATED: `-5`!>xor(-8, 3)<!>
const konst inv = <!EVALUATED: `-9`!>inv(8)<!>

const konst a1 = <!EVALUATED: `1`!>toByte(1)<!>
const konst a2 = <!EVALUATED: ``!>toChar(2)<!>
const konst a3 = <!EVALUATED: `3`!>toShort(3)<!>
const konst a4 = <!EVALUATED: `4`!>toInt(4)<!>
const konst a5 = <!EVALUATED: `5`!>toLong(5)<!>
const konst a6 = <!EVALUATED: `6.0`!>toFloat(6)<!>
const konst a7 = <!EVALUATED: `7.0`!>toDouble(7)<!>

const konst b1 = <!EVALUATED: `10`!>toString(10)<!>
const konst b2 = <!EVALUATED: `10`!>hashCode(10)<!>
const konst b3 = <!EVALUATED: `false`!>equals(10, 11)<!>
const konst b4 = <!EVALUATED: `true`!>equals(1, 1.toInt())<!>
const konst b5 = <!EVALUATED: `true`!>equals(1, 1)<!>
