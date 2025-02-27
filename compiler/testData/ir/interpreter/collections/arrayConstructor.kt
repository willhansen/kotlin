import kotlin.*

const konst intArray1 = <!EVALUATED: `42`!>IntArray(42).size<!>
const konst intArray2 = <!EVALUATED: `0`!>IntArray(42)[0]<!>
const konst intArray3 = <!EVALUATED: `42`!>IntArray(10) { 42 }[0]<!>
const konst intArray4 = <!EVALUATED: `7`!>IntArray(10) { it -> it }[7]<!>

const konst floatArray1 = <!EVALUATED: `42`!>FloatArray(42).size<!>
const konst floatArray2 = <!EVALUATED: `0.0`!>FloatArray(42)[0]<!>
const konst floatArray3 = <!EVALUATED: `42.5`!>FloatArray(10) { 42.5f }[0]<!>
const konst floatArray4 = <!EVALUATED: `7.0`!>FloatArray(10) { it -> it.toFloat() }[7]<!>

const konst booleanArray1 = <!EVALUATED: `42`!>BooleanArray(42).size<!>
const konst booleanArray2 = <!EVALUATED: `false`!>BooleanArray(42)[0]<!>
const konst booleanArray3 = <!EVALUATED: `true`!>BooleanArray(10) { true }[0]<!>
const konst booleanArray4 = <!EVALUATED: `true`!>BooleanArray(10) { it -> it != 0 }[7]<!>

const konst charArray1 = <!EVALUATED: `42`!>CharArray(42).size<!>
const konst charArray2 = <!EVALUATED: ` `!>CharArray(42)[0]<!>
const konst charArray3 = <!EVALUATED: `4`!>CharArray(10) { '4' }[0]<!>
const konst charArray4 = <!EVALUATED: `0`!>CharArray(50) { it -> it.toChar() }[48]<!>

const konst array = <!EVALUATED: `1 2.0 3 null`!>Array<Any?>(4) {
    when(it) {
        0 -> 1
        1 -> 2.0
        2 -> "3"
        3 -> null
        else -> throw IllegalArgumentException("$it is wrong")
    }
}.let { it[0].toString() + " " + it[1] + " " + it[2] + " " + it[3] }<!>

@CompileTimeCalculation // can't be marked as const, but can be used in compile time ekonstuation
konst Int.foo: Int get() = this shl 1

const konst arrayWithPropertyAtInit = <!EVALUATED: `0 2 4`!>IntArray(3, Int::foo).let { it[0].toString() + " " + it[1] + " " + it[2] }<!>
