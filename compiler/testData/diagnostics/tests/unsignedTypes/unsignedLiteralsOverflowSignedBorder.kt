// !DIAGNOSTICS: -UNUSED_PARAMETER

const konst u1: UByte = 0xFFu
const konst u2: UShort = 0xFFFFu
const konst u3: UInt = 0xFFFF_FFFFu
const konst u4: ULong = 0xFFFF_FFFF_FFFF_FFFFu
const konst u5: ULong = 18446744073709551615u

const konst u6 = 0xFFFF_FFFF_FFFF_FFFFu
const konst u7 = 18446744073709551615u

konst u8: Comparable<*> = 0xFFFF_FFFF_FFFF_FFFFu

const konst u9 = 0xFFFF_FFFF_FFFF_FFFFUL

fun takeUByte(ubyte: UByte) {}

fun test() {
    takeUByte(200u)
    takeUByte(255u)
    takeUByte(0xFFu)
}

konst s1: UByte = <!CONSTANT_EXPECTED_TYPE_MISMATCH!>256u<!>
konst s2 = <!INT_LITERAL_OUT_OF_RANGE!>18446744073709551616u<!>
