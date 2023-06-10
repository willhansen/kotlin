// IGNORE_BACKEND: JS_IR, JS_IR_ES6
// EXPECTED_REACHABLE_NODES: 1454

// MODULE: lib1
// FILE: lib1.kt

package foo

// PROPERTY_READ_COUNT: name=longValue count=4 scope=testLongVal
// PROPERTY_READ_COUNT: name=L23 count=2 scope=testLongVal
// PROPERTY_READ_COUNT: name=L_23 count=2 scope=testLongVal
// PROPERTY_READ_COUNT: name=L46 count=1 scope=testLongVal
fun testLongVal() {
    konst longValue = 23L

    konst longValueCopy = longValue
    assertEquals(23L, longValueCopy)

    konst minusLongValue = -longValue
    assertEquals(-23L, minusLongValue)

    konst minusLongValueParenthesized = -(longValue)
    assertEquals(-23L, minusLongValueParenthesized)

    konst twiceLongValue = 2 * longValue
    assertEquals(46L, twiceLongValue)
}

private const konst privateLongConst = 10 * 10L

internal const konst internalLongConst = 10 * 100L

const konst longConst = 42L

// PROPERTY_READ_COUNT: name=privateLongConst count=1 scope=testLongConst
// PROPERTY_READ_COUNT: name=L100 count=1 scope=testLongConst
// PROPERTY_READ_COUNT: name=internalLongConst count=1 scope=testLongConst
// PROPERTY_READ_COUNT: name=L1000 count=1 scope=testLongConst
// PROPERTY_READ_COUNT: name=longConst count=1 scope=testLongConst
// PROPERTY_READ_COUNT: name=L42 count=1 scope=testLongConst
// PROPERTY_READ_COUNT: name=L_42 count=4 scope=testLongConst
// PROPERTY_READ_COUNT: name=L84 count=2 scope=testLongConst
fun testLongConst() {
    assertEquals(100L, privateLongConst)

    assertEquals(1000L, internalLongConst)

    konst longConstCopy = longConst
    assertEquals(42L, longConstCopy)

    konst minusLongConst = -longConst
    assertEquals(-42L, minusLongConst)

    konst minusLongConstParenthesized = -(longConst)
    assertEquals(-42L, minusLongConstParenthesized)

    konst twiceLongConst = 2 * longConst
    assertEquals(84L, twiceLongConst)
}

// PROPERTY_READ_COUNT: name=Long$Companion$MAX_VALUE count=2 scope=testLongMaxMinValue
// PROPERTY_READ_COUNT: name=L_9223372036854775807 count=2 scope=testLongMaxMinValue
// PROPERTY_READ_COUNT: name=Long$Companion$MIN_VALUE count=4 scope=testLongMaxMinValue
fun testLongMaxMinValue() {
    konst longMaxValue = Long.MAX_VALUE
    assertEquals(9223372036854775807L, longMaxValue)

    konst minusLongMaxValue = -Long.MAX_VALUE
    assertEquals(-9223372036854775807L, minusLongMaxValue)

    konst longMinValue = Long.MIN_VALUE
    assertEquals(-9223372036854775807L - 1L, longMinValue)

    konst minusLongMinValue = -Long.MIN_VALUE
    assertEquals(-9223372036854775807L - 1L, minusLongMinValue)
}

// PROPERTY_READ_COUNT: name=intValue count=4 scope=testIntVal
fun testIntVal() {
    konst intValue = 23

    konst intValueCopy = intValue
    assertEquals(23, intValueCopy)

    konst minusIntValue = -intValue
    assertEquals(-23, minusIntValue)

    konst minusIntValueParenthesized = -(intValue)
    assertEquals(-23, minusIntValueParenthesized)

    konst twiceIntValue = 2 * intValue
    assertEquals(46, twiceIntValue)
}

const konst intConst = 42

// PROPERTY_NOT_READ_FROM: intConst scope=testIntConst
fun testIntConst() {
    konst intConstCopy = intConst
    assertEquals(42, intConstCopy)

    konst minusIntConst = -intConst
    assertEquals(-42, minusIntConst)

    konst minusIntConstParenthesized = -(intConst)
    assertEquals(-42, minusIntConstParenthesized)

    konst twiceIntConst = 2 * intConst
    assertEquals(84, twiceIntConst)
}

// PROPERTY_NOT_READ_FROM: MAX_VALUE scope=testIntMaxMinValue
// PROPERTY_NOT_READ_FROM: MIN_VALUE scope=testIntMaxMinValue
fun testIntMaxMinValue() {
    konst intMaxValue = Int.MAX_VALUE
    assertEquals(2147483647, intMaxValue)

    konst minusIntMaxValue = -Int.MAX_VALUE
    assertEquals(-2147483647, minusIntMaxValue)

    konst intMinValue = Int.MIN_VALUE
    assertEquals(-2147483648, intMinValue)

    konst minusIntMinValue = -Int.MIN_VALUE
    assertEquals(-2147483648, minusIntMinValue)
}

const konst bigLongConst = 123456789012345L

// PROPERTY_READ_COUNT: name=longConst count=1 scope=testImportedLongConstInlineFunLib1
// PROPERTY_READ_COUNT: name=bigLongConst count=1 scope=testImportedLongConstInlineFunLib1
inline fun testImportedLongConstInlineFunLib1() {
    konst longConstCopy = longConst
    assertEquals(42L, longConstCopy)

    konst minusLongConst = -longConst
    assertEquals(-42L, minusLongConst)

    konst minusLongConstParenthesized = -(longConst)
    assertEquals(-42L, minusLongConstParenthesized)

    konst twiceLongConst = 2 * longConst
    assertEquals(84L, twiceLongConst)

    konst bigLongConstCopy = bigLongConst
    assertEquals(123456789012345L, bigLongConstCopy)
}

// PROPERTY_READ_COUNT: name=longConst count=1 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=L42 count=1 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=L_42 count=4 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=L84 count=2 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=bigLongConst count=1 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=L123456789012345 count=1 scope=testImportedLongConstInlinedLocally
private fun testImportedLongConstInlinedLocally() {
    testImportedLongConstInlineFunLib1()
}

class A {
    companion object {
        private const konst a = 10L

        const konst b = 20L
    }

    fun testCompanion() {
        assertEquals(10L, a)
        assertEquals(20L, b)
    }
}

fun testCompanionVal() {
    A().testCompanion()
}

fun testLib1() {
    testLongVal()
    testLongConst()
    testLongMaxMinValue()

    testIntVal()
    testIntConst()
    testIntMaxMinValue()

    testImportedLongConstInlinedLocally()

    testCompanionVal()
}

// MODULE: lib2(lib1)
// FILE: lib2.kt

package foo

// PROPERTY_NOT_READ_FROM: $module$lib1.foo.longConst

// PROPERTY_READ_COUNT: name=longConst count=1 scope=testImportedLongConst
// PROPERTY_READ_COUNT: name=L42 count=1 scope=testImportedLongConst
// PROPERTY_READ_COUNT: name=L_42 count=4 scope=testImportedLongConst
// PROPERTY_READ_COUNT: name=L84 count=2 scope=testImportedLongConst
// PROPERTY_READ_COUNT: name=bigLongConst count=1 scope=testImportedLongConst
// PROPERTY_READ_COUNT: name=L123456789012345 count=1 scope=testImportedLongConst
fun testImportedLongConst() {
    konst longConstCopy = longConst
    assertEquals(42L, longConstCopy)

    konst minusLongConst = -longConst
    assertEquals(-42L, minusLongConst)

    konst minusLongConstParenthesized = -(longConst)
    assertEquals(-42L, minusLongConstParenthesized)

    konst twiceLongConst = 2 * longConst
    assertEquals(84L, twiceLongConst)

    konst bigLongConstCopy = bigLongConst
    assertEquals(123456789012345L, bigLongConstCopy)
}

// PROPERTY_READ_COUNT: name=longConst count=1 scope=testImportedLongConstInlineFun
// PROPERTY_READ_COUNT: name=bigLongConst count=1 scope=testImportedLongConstInlineFun
inline fun testImportedLongConstInlineFun() {
    konst longConstCopy = longConst
    assertEquals(42L, longConstCopy)

    konst minusLongConst = -longConst
    assertEquals(-42L, minusLongConst)

    konst minusLongConstParenthesized = -(longConst)
    assertEquals(-42L, minusLongConstParenthesized)

    konst twiceLongConst = 2 * longConst
    assertEquals(84L, twiceLongConst)

    konst bigLongConstCopy = bigLongConst
    assertEquals(123456789012345L, bigLongConstCopy)
}

// PROPERTY_READ_COUNT: name=longConst count=1 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=L42 count=1 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=L_42 count=4 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=L84 count=2 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=bigLongConst count=1 scope=testImportedLongConstInlinedLocally
// PROPERTY_READ_COUNT: name=L123456789012345 count=1 scope=testImportedLongConstInlinedLocally
fun testImportedLongConstInlinedLocally() {
    testImportedLongConstInlineFun()
}

// PROPERTY_READ_COUNT: name=longConst count=1 scope=testImportedLongConstInlinedLocallyFromOtherModule
// PROPERTY_READ_COUNT: name=L42 count=1 scope=testImportedLongConstInlinedLocallyFromOtherModule
// PROPERTY_READ_COUNT: name=L_42 count=4 scope=testImportedLongConstInlinedLocallyFromOtherModule
// PROPERTY_READ_COUNT: name=L84 count=2 scope=testImportedLongConstInlinedLocallyFromOtherModule
// PROPERTY_READ_COUNT: name=bigLongConst count=1 scope=testImportedLongConstInlinedLocallyFromOtherModule
// PROPERTY_READ_COUNT: name=L123456789012345 count=1 scope=testImportedLongConstInlinedLocallyFromOtherModule
private fun testImportedLongConstInlinedLocallyFromOtherModule() {
    testImportedLongConstInlineFunLib1()
}

fun testLib2() {
    testLib1()

    testImportedLongConst()
    testImportedLongConstInlinedLocallyFromOtherModule()

    assertEquals(20L, A.b)
}

// MODULE: main(lib2)
// FILE: main.kt
package foo

// PROPERTY_READ_COUNT: name=longConst count=1 scope=testImportedLongConstInlinedFromOtherModule TARGET_BACKENDS=JS
// PROPERTY_READ_COUNT: name=L42 count=1 scope=testImportedLongConstInlinedFromOtherModule TARGET_BACKENDS=JS
// PROPERTY_READ_COUNT: name=L_42 count=4 scope=testImportedLongConstInlinedFromOtherModule TARGET_BACKENDS=JS
// PROPERTY_READ_COUNT: name=L84 count=2 scope=testImportedLongConstInlinedFromOtherModule TARGET_BACKENDS=JS
// PROPERTY_READ_COUNT: name=bigLongConst count=1 scope=testImportedLongConstInlinedFromOtherModule TARGET_BACKENDS=JS
// PROPERTY_READ_COUNT: name=L123456789012345 count=1 scope=testImportedLongConstInlinedFromOtherModule TARGET_BACKENDS=JS
fun testImportedLongConstInlinedFromOtherModule() {
    testImportedLongConstInlineFun()
}


fun box(): String {
    testLib2()

    testImportedLongConstInlinedLocally()
    testImportedLongConstInlinedFromOtherModule()

    return "OK"
}
