// WITH_STDLIB
package test

// konst x1: 3
<!DEBUG_INFO_CONSTANT_VALUE("3")!>konst x1 = 1 + 2<!>

// konst x2: 3.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("3.toLong()")!>konst x2 = 1 + 2L<!>

// konst x3: 3
<!DEBUG_INFO_CONSTANT_VALUE("3")!>konst x3 = 1.toShort() + 2.toByte()<!>

// konst x4: 3
<!DEBUG_INFO_CONSTANT_VALUE("3")!>konst x4 = 1.toByte() + 2.toByte()<!>

// konst x5: 4656
<!DEBUG_INFO_CONSTANT_VALUE("4656")!>konst x5 = 0x1234 and 0x5678<!>

// Strange result, see KT-13517
// konst x6: null
<!DEBUG_INFO_CONSTANT_VALUE("null")!>konst x6 = 0x1234 and <!CONSTANT_EXPECTED_TYPE_MISMATCH!>0x5678L<!><!>

// konst x7: 4656.toLong()
<!DEBUG_INFO_CONSTANT_VALUE("4656.toLong()")!>konst x7 = 0x1234L and 0x5678<!>

// konst x8: -123457
<!DEBUG_INFO_CONSTANT_VALUE("-123457")!>konst x8 = (-123_456_789_321).floorDiv(1_000_000)<!>

// konst x9: 79
<!DEBUG_INFO_CONSTANT_VALUE("79")!>konst x9 = (-123_456_789_321).mod(100)<!>
