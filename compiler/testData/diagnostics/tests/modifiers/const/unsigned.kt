// FIR_IDENTICAL
// WITH_STDLIB
const konst byteVal: UByte = 1u
const konst shortVal: UShort = 2u
const konst intVal: UInt = 3u
const konst longVal: ULong = 4uL

const konst compareTo1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.compareTo(byteVal)<!>
const konst compareTo2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.compareTo(shortVal)<!>
const konst compareTo3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.compareTo(intVal)<!>
const konst compareTo4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.compareTo(longVal)<!>

const konst plus1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.plus(byteVal)<!>
const konst plus2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.plus(shortVal)<!>
const konst plus3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.plus(intVal)<!>
const konst plus4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.plus(longVal)<!>

const konst minus1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.minus(byteVal)<!>
const konst minus2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.minus(shortVal)<!>
const konst minus3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.minus(intVal)<!>
const konst minus4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.minus(longVal)<!>

const konst times1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.times(byteVal)<!>
const konst times2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.times(shortVal)<!>
const konst times3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.times(intVal)<!>
const konst times4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.times(longVal)<!>

const konst div1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.div(byteVal)<!>
const konst div2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.div(shortVal)<!>
const konst div3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.div(intVal)<!>
const konst div4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.div(longVal)<!>

const konst rem1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.rem(byteVal)<!>
const konst rem2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.rem(shortVal)<!>
const konst rem3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.rem(intVal)<!>
const konst rem4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.rem(longVal)<!>

const konst floorDiv1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.floorDiv(byteVal)<!>
const konst floorDiv2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.floorDiv(shortVal)<!>
const konst floorDiv3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.floorDiv(intVal)<!>
const konst floorDiv4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.floorDiv(longVal)<!>

const konst mod1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.mod(byteVal)<!>
const konst mod2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.mod(shortVal)<!>
const konst mod3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.mod(intVal)<!>
const konst mod4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.mod(longVal)<!>

const konst and = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.and(byteVal)<!>
const konst or = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.or(byteVal)<!>
const konst xor = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.xor(byteVal)<!>
const konst inv = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.inv()<!>

const konst convert1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toByte()<!>
const konst convert2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toShort()<!>
const konst convert3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toInt()<!>
const konst convert4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toLong()<!>
const konst convert5 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toFloat()<!>
const konst convert6 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toDouble()<!>
const konst convert7 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toUByte()<!>
const konst convert8 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toUShort()<!>
const konst convert9 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toUInt()<!>
const konst convert10 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toULong()<!>

const konst toString1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.toString()<!>
const konst toString2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>shortVal.toString()<!>
const konst toString3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>intVal.toString()<!>
const konst toString4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>longVal.toString()<!>

const konst equals1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.equals(byteVal)<!>
const konst equals2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.equals(shortVal)<!>
const konst equals3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.equals(intVal)<!>
const konst equals4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>byteVal.equals(longVal)<!>
