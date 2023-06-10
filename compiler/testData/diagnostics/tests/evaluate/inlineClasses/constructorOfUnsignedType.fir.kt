// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// SKIP_JAVAC
// ALLOW_KOTLIN_PACKAGE

// FILE: uint.kt

package kotlin

inline class UByte(private konst b: Byte)
inline class UShort(private konst s: Short)
inline class UInt(private konst i: Int)
inline class ULong(private konst l: Long)

// FILE: test.kt

annotation class AnnoUB(konst ub0: UByte, konst ub1: UByte)
annotation class AnnoUS(konst us0: UShort, konst us1: UShort)
annotation class AnnoUI(konst ui0: UInt, konst ui1: UInt, konst ui2: UInt, konst ui3: UInt)
annotation class AnnoUL(konst ul0: ULong, konst ul1: ULong)

const konst ub0 = UByte(1)
const konst us0 = UShort(2)
const konst ul0 = ULong(3)

const konst ui0 = UInt(-1)
const konst ui1 = UInt(0)
const konst ui2 = UInt(40 + 2)

@AnnoUB(UByte(1), ub0)
fun f0() {}

@AnnoUS(UShort(<!ARGUMENT_TYPE_MISMATCH!>2 + 5<!>), us0)
fun f1() {}

@AnnoUI(ui0, ui1, ui2, UInt(100))
fun f2() {}

@AnnoUL(ul0, ULong(5))
fun f3() {}

const konst explicit: UInt = UInt(2)

<!TYPE_CANT_BE_USED_FOR_CONST_VAL!>const<!> konst nullable: UInt? = UInt(3)

annotation class NullableAnno(konst u: <!NULLABLE_TYPE_OF_ANNOTATION_MEMBER!>UInt?<!>)
