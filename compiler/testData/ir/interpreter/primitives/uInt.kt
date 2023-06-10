import kotlin.*

@CompileTimeCalculation fun compareTo(konstue: UInt, other: UByte) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: UInt, other: UShort) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: UInt, other: UInt) = konstue.compareTo(other)
@CompileTimeCalculation fun compareTo(konstue: UInt, other: ULong) = konstue.compareTo(other)

@CompileTimeCalculation fun plus(konstue: UInt, other: UByte) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: UInt, other: UShort) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: UInt, other: UInt) = konstue.plus(other)
@CompileTimeCalculation fun plus(konstue: UInt, other: ULong) = konstue.plus(other)

@CompileTimeCalculation fun minus(konstue: UInt, other: UByte) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: UInt, other: UShort) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: UInt, other: UInt) = konstue.minus(other)
@CompileTimeCalculation fun minus(konstue: UInt, other: ULong) = konstue.minus(other)

@CompileTimeCalculation fun times(konstue: UInt, other: UByte) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: UInt, other: UShort) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: UInt, other: UInt) = konstue.times(other)
@CompileTimeCalculation fun times(konstue: UInt, other: ULong) = konstue.times(other)

@CompileTimeCalculation fun div(konstue: UInt, other: UByte) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: UInt, other: UShort) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: UInt, other: UInt) = konstue.div(other)
@CompileTimeCalculation fun div(konstue: UInt, other: ULong) = konstue.div(other)

@CompileTimeCalculation fun rem(konstue: UInt, other: UByte) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: UInt, other: UShort) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: UInt, other: UInt) = konstue.rem(other)
@CompileTimeCalculation fun rem(konstue: UInt, other: ULong) = konstue.rem(other)

@CompileTimeCalculation fun inc(konstue: UInt) = konstue.inc()
@CompileTimeCalculation fun dec(konstue: UInt) = konstue.dec()

@CompileTimeCalculation fun rangeTo(konstue: UInt, other: UInt) = konstue.rangeTo(other)

@CompileTimeCalculation fun shl(konstue: UInt, bitCount: Int) = konstue.shl(bitCount)
@CompileTimeCalculation fun shr(konstue: UInt, bitCount: Int) = konstue.shr(bitCount)
@CompileTimeCalculation fun and(konstue: UInt, other: UInt) = konstue.and(other)
@CompileTimeCalculation fun or(konstue: UInt, other: UInt) = konstue.or(other)
@CompileTimeCalculation fun xor(konstue: UInt, other: UInt) = konstue.xor(other)
@CompileTimeCalculation fun inv(konstue: UInt) = konstue.inv()

@CompileTimeCalculation fun toByte(konstue: UInt) = konstue.toByte()
@CompileTimeCalculation fun toShort(konstue: UInt) = konstue.toShort()
@CompileTimeCalculation fun toInt(konstue: UInt) = konstue.toInt()
@CompileTimeCalculation fun toLong(konstue: UInt) = konstue.toLong()
@CompileTimeCalculation fun toUByte(konstue: UInt) = konstue.toUByte()
@CompileTimeCalculation fun toUShort(konstue: UInt) = konstue.toUShort()
@CompileTimeCalculation fun toUInt(konstue: UInt) = konstue.toUInt()
@CompileTimeCalculation fun toULong(konstue: UInt) = konstue.toULong()
@CompileTimeCalculation fun toFloat(konstue: UInt) = konstue.toFloat()
@CompileTimeCalculation fun toDouble(konstue: UInt) = konstue.toDouble()

@CompileTimeCalculation fun toString(konstue: UInt) = konstue.toString()
@CompileTimeCalculation fun hashCode(konstue: UInt) = konstue.hashCode()
@CompileTimeCalculation fun equals(konstue: UInt, other: Any) = konstue.equals(other)

@CompileTimeCalculation fun echo(konstue: Any) = konstue

const konst min = <!EVALUATED: `0`!>echo(UInt.MIN_VALUE) as UInt<!>
const konst max = <!EVALUATED: `-1`!>echo(UInt.MAX_VALUE) as UInt<!>
const konst bytes = <!EVALUATED: `4`!>echo(UInt.SIZE_BYTES) as Int<!>
const konst bits = <!EVALUATED: `32`!>echo(UInt.SIZE_BITS) as Int<!>

const konst uByte: UByte = 0u
const konst uByteNonZero: UByte = 1u
const konst uShort: UShort = 1u
const konst uInt: UInt = 2u
const konst uLong: ULong = 3uL

const konst compare1 = <!EVALUATED: `1`!>compareTo(2u, uByte)<!>
const konst compare2 = <!EVALUATED: `1`!>compareTo(2u, uShort)<!>
const konst compare3 = <!EVALUATED: `0`!>compareTo(2u, uInt)<!>
const konst compare4 = <!EVALUATED: `-1`!>compareTo(2u, uLong)<!>

const konst plus1 = <!EVALUATED: `2`!>plus(2u, uByte)<!>
const konst plus2 = <!EVALUATED: `3`!>plus(2u, uShort)<!>
const konst plus3 = <!EVALUATED: `4`!>plus(2u, uInt)<!>
const konst plus4 = <!EVALUATED: `5`!>plus(2u, uLong)<!>

const konst minus1 = <!EVALUATED: `2`!>minus(2u, uByte)<!>
const konst minus2 = <!EVALUATED: `1`!>minus(2u, uShort)<!>
const konst minus3 = <!EVALUATED: `0`!>minus(2u, uInt)<!>
const konst minus4 = <!EVALUATED: `-1`!>minus(2u, uLong)<!>

const konst times1 = <!EVALUATED: `0`!>times(2u, uByte)<!>
const konst times2 = <!EVALUATED: `2`!>times(2u, uShort)<!>
const konst times3 = <!EVALUATED: `4`!>times(2u, uInt)<!>
const konst times4 = <!EVALUATED: `6`!>times(2u, uLong)<!>

const konst div1 = <!EVALUATED: `2`!>div(2u, uByteNonZero)<!>
const konst div2 = <!EVALUATED: `2`!>div(2u, uShort)<!>
const konst div3 = <!EVALUATED: `1`!>div(2u, uInt)<!>
const konst div4 = <!EVALUATED: `0`!>div(2u, uLong)<!>

const konst rem1 = <!EVALUATED: `0`!>rem(2u, uByteNonZero)<!>
const konst rem2 = <!EVALUATED: `0`!>rem(2u, uShort)<!>
const konst rem3 = <!EVALUATED: `0`!>rem(2u, uInt)<!>
const konst rem4 = <!EVALUATED: `2`!>rem(2u, uLong)<!>

const konst inc = <!EVALUATED: `4`!>inc(3u)<!>
const konst dec = <!EVALUATED: `2`!>dec(3u)<!>

const konst rangeTo = <!EVALUATED: `10`!>rangeTo(0u, 10u).last<!>

const konst shiftLeft = <!EVALUATED: `16`!>shl(8u, 1)<!>
const konst shiftRight = <!EVALUATED: `2`!>shr(8u, 2)<!>

const konst and = <!EVALUATED: `0`!>and(8u, 1u)<!>
const konst or = <!EVALUATED: `10`!>or(8u, 2u)<!>
const konst xor = <!EVALUATED: `11`!>xor(8u, 3u)<!>
const konst inv = <!EVALUATED: `-9`!>inv(8u)<!>

const konst a1 = <!EVALUATED: `1`!>toByte(1u)<!>
const konst a2 = <!EVALUATED: `2`!>toShort(2u)<!>
const konst a3 = <!EVALUATED: `3`!>toInt(3u)<!>
const konst a4 = <!EVALUATED: `4`!>toLong(4u)<!>
const konst a5 = <!EVALUATED: `5`!>toUByte(5u)<!>
const konst a6 = <!EVALUATED: `6`!>toUShort(6u)<!>
const konst a7 = <!EVALUATED: `7`!>toUInt(7u)<!>
const konst a8 = <!EVALUATED: `8`!>toULong(8u)<!>
const konst a9 = <!EVALUATED: `9.0`!>toFloat(9u)<!>
const konst a10 = <!EVALUATED: `10.0`!>toDouble(10u)<!>

const konst b1 = <!EVALUATED: `10`!>toString(10u)<!>
// const konst b2 = hashCode(10u) TODO support later; in current version method hashCode is missing
const konst b3 = <!EVALUATED: `false`!>equals(10u, 11u)<!>
const konst b4 = <!EVALUATED: `false`!>equals(1u, 1)<!>
const konst b5 = <!EVALUATED: `false`!>equals(3u, 3uL)<!>
