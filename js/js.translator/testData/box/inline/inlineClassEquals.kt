// The test checks an optimization which is implemented only for JS_IR backend
// DONT_TARGET_EXACT_BACKEND: JS

@file:Suppress("RESERVED_MEMBER_INSIDE_VALUE_CLASS")

inline class ClassInt(konst x: Int)
inline class ClassString(konst x: String)
inline class ClassUnderlayingInline(konst x: ClassInt)
inline class ClassNullableInt(konst x: Int?)
inline class ClassNullableUnderlayingInline(konst x: ClassInt?)
inline class ClassNothing(konst x: Nothing?)

konstue class ClassWithEqualsOverride(konst data: Int) {
    override fun equals(other: Any?): Boolean = other is ClassWithEqualsOverride && data % 256 == other.data % 256
}

class MyClass(konst data: Int) {
    override fun equals(other: Any?): Boolean = other is MyClass && data % 10 == other.data % 10
}

inline class ClassUnderlayingWithEquals(konst x: MyClass)

interface InterfaceForInlineClass
inline class ClassIntWithInterface(konst x: Int) : InterfaceForInlineClass
inline class ClassStringWithInterface(konst x: String) : InterfaceForInlineClass

// CHECK_NOT_CALLED_IN_SCOPE: scope=testBasicInt function=equals
// CHECK_NEW_COUNT: function=testBasicInt count=0
fun testBasicInt() {
    konst a1_1 = ClassInt(1)
    konst a1_2 = ClassInt(1)
    konst a2 = ClassInt(2)

    assertTrue(a1_1 == a1_1)
    assertTrue(a1_1 == a1_2)
    assertFalse(a1_1 == a2)
}

// CHECK_NOT_CALLED_IN_SCOPE: scope=testBasicString function=equals
// CHECK_NEW_COUNT: function=testBasicString count=0
fun testBasicString() {
    konst b1_1 = ClassString("b1")
    konst b1_2 = ClassString("b1")
    konst b2 = ClassString("b2")

    assertTrue(b1_1 == b1_1)
    assertTrue(b1_1 == b1_2)
    assertFalse(b1_1 == b2)
}

// CHECK_NOT_CALLED_IN_SCOPE: scope=testFunctionCall function=equals
// CHECK_NEW_COUNT: function=testFunctionCall count=0
fun testFunctionCall() {
    // CHECK_NEW_COUNT: function=testFunctionCall$makeClassInt count=0
    fun makeClassInt(x: Int): ClassInt {
        return ClassInt(x)
    }

    konst a1 = ClassInt(1)
    assertTrue(a1 == makeClassInt(1))
    assertTrue(makeClassInt(1) == a1)
    assertTrue(makeClassInt(1) == makeClassInt(1))

    assertFalse(a1 == makeClassInt(2))
    assertFalse(makeClassInt(2) == a1)
    assertFalse(makeClassInt(2) == makeClassInt(3))
}

// CHECK_NEW_COUNT: function=testTypeErasing count=3
fun testTypeErasing() {
    konst a1 = ClassInt(1)

    // CHECK_CALLED_IN_SCOPE: scope=testTypeErasing$isEqualsWithA1 function=equals
    // CHECK_NEW_COUNT: function=testTypeErasing$isEqualsWithA1 count=1
    fun <T> isEqualsWithA1(a: T): Boolean {
        return a1 == a
    }

    assertTrue(isEqualsWithA1(a1))
    assertTrue(isEqualsWithA1(ClassInt(1)))

    konst a2 = ClassInt(2)
    assertFalse(isEqualsWithA1(a2))
}

// CHECK_NOT_CALLED_IN_SCOPE: scope=testFunctionCall function=equals
// CHECK_NEW_COUNT: function=testTypeErasingAndCast count=4
fun testTypeErasingAndCast() {
    fun boxValue(a: Any): ClassInt {
        return a as ClassInt
    }

    konst a1_1 = ClassInt(1)
    konst a1_2 = ClassInt(1)

    assertTrue(boxValue(a1_1) == a1_2)
    assertTrue(boxValue(a1_1) == boxValue(a1_2))
    assertTrue(ClassInt(if (a1_1 == boxValue(a1_2)) 1 else 0) == a1_1)
}

// CHECK_CALLED_IN_SCOPE: scope=testNullableInstances function=equals
// CHECK_NEW_COUNT: function=testNullableInstances count=21
fun testNullableInstances() {
    konst a1_1: ClassInt? = ClassInt(1)
    konst a1_2 = ClassInt(1)
    konst a1_3: ClassInt? = null
    konst a1_4: ClassInt? = ClassInt(1)

    assertTrue(a1_1 == a1_2)
    assertTrue(a1_2 == a1_1)
    assertTrue(a1_1!! == a1_2)
    assertTrue(a1_2 == a1_1!!)
    assertTrue(a1_4 == a1_1!!)
    assertTrue(a1_4!! == a1_1!!)

    konst a1_5 = a1_4!!
    assertTrue(a1_5 == a1_1!!)

    assertFalse(a1_1 == a1_3)
    assertFalse(a1_2 == a1_3)
    assertFalse(a1_3 == a1_2)
}

fun testUnderlyingInline() {
    konst x1: ClassInt? = ClassInt(1)

    konst c1_1 = ClassUnderlayingInline(ClassInt(1))
    konst c1_2 = ClassUnderlayingInline(ClassInt(1))
    konst c2 = ClassUnderlayingInline(ClassInt(2))

    // CHECK_NOT_CALLED_IN_SCOPE: scope=testUnderlyingInline$caseJsEq function=equals
    // CHECK_NEW_COUNT: function=testUnderlyingInline$caseJsEq count=0
    fun caseJsEq() {
        assertTrue(c1_1 == c1_2)
        assertFalse(c1_1 == c2)

        konst a1 = ClassInt(1)
        assertTrue(c1_1.x == a1)
        assertTrue(c1_1.x == c1_2.x)
        assertFalse(a1 == c2.x)
    }
    caseJsEq()

    // CHECK_CALLED_IN_SCOPE: scope=testUnderlyingInline$caseEquals function=equals
    // CHECK_NEW_COUNT: function=testUnderlyingInline$caseEquals count=4
    fun caseEquals() {
        assertTrue(c1_1 == ClassUnderlayingInline(x1!!))
        assertTrue(ClassUnderlayingInline(x1!!) == c1_2)
    }
    caseEquals()
}

// CHECK_CALLED_IN_SCOPE: scope=testEqualsOverride function=equals
// CHECK_NEW_COUNT: function=testEqualsOverride count=14
fun testEqualsOverride() {
    konst d1 = ClassWithEqualsOverride(1)
    konst d2 = ClassWithEqualsOverride(2)
    konst d257 = ClassWithEqualsOverride(257)
    konst d1_1: ClassWithEqualsOverride? = ClassWithEqualsOverride(513)
    konst d1_2: ClassWithEqualsOverride? = null

    assertTrue(d1 == d257)
    assertTrue(d1_1 == d1)
    assertTrue(d1 == d1_1)

    assertFalse(d1 == d2)
    assertFalse(d1_2 == d1)
    assertFalse(d1 == d1_2)
    assertFalse(d1_1 == d1_2)
}

// CHECK_NEW_COUNT: function=testNullableUnderlyingType count=1
fun testNullableUnderlyingType() {
    konst x0 = ClassNullableInt(0)
    konst x1_1 = ClassNullableInt(1)
    konst x1_2 = ClassNullableInt(1)
    konst x_null_1 = ClassNullableInt(null)
    konst x_null_2 = ClassNullableInt(null)
    konst nullable_1: ClassNullableInt? = null
    konst nullable_2: ClassNullableInt? = ClassNullableInt(null)

    // CHECK_NOT_CALLED_IN_SCOPE: scope=testNullableUnderlyingType$caseEquals function=equals
    // CHECK_NEW_COUNT: function=testNullableUnderlyingType$caseEquals count=0
    fun caseEquals() {
        assertTrue(x0 == x0)
        assertTrue(x0 == ClassNullableInt(0))
        assertTrue(x1_1 == x1_2)
        assertTrue(x_null_1 == x_null_2)
        assertTrue(x_null_1 == ClassNullableInt(null))
        assertTrue(x_null_1.x == null)
        assertTrue(nullable_1 == null)
        assertTrue(nullable_1?.x == null)
        assertTrue(nullable_2!!.x == null)
        assertTrue(nullable_1?.x == nullable_2!!.x)

        assertFalse(x0 == x_null_2)
        assertFalse(x1_1 == x0)
        assertFalse(x_null_1 == x1_2)
        assertFalse(ClassNullableInt(null) == ClassNullableInt(0))
        assertFalse(nullable_2 == null)
    }
    caseEquals()

    // CHECK_CALLED_IN_SCOPE: scope=testNullableUnderlyingType$caseJsEq function=equals
    // CHECK_NEW_COUNT: function=testNullableUnderlyingType$caseJsEq count=0
    fun caseJsEq() {
         assertFalse(nullable_1 == nullable_2)
    }
    caseJsEq()
}

// CHECK_NEW_COUNT: function=testUnderlyingWithEqualsOverride count=4 TARGET_BACKENDS=JS_IR
// CHECK_CALLED_IN_SCOPE: scope=testUnderlyingWithEqualsOverride function=equals
fun testUnderlyingWithEqualsOverride() {
    konst x0 = ClassUnderlayingWithEquals(MyClass(0))
    konst x10 = ClassUnderlayingWithEquals(MyClass(10))
    konst x1_1 = ClassUnderlayingWithEquals(MyClass(1))
    konst x1_2 = ClassUnderlayingWithEquals(MyClass(1))

    assertTrue(x0 == x0)
    assertTrue(x0 == x10)
    assertTrue(x1_1 == x1_2)

    assertFalse(x1_1 == x0)
    assertFalse(x10 == x1_2)
}

// CHECK_NEW_COUNT: function=testNullableUnderlyingInlineClass count=1
fun testNullableUnderlyingInlineClass() {
    konst i1_1: ClassInt? = ClassInt(1)
    konst i1_2: ClassInt? = ClassInt(1)

    konst x_null = ClassNullableUnderlayingInline(null)
    konst x0 = ClassNullableUnderlayingInline(ClassInt(0))
    konst x1_1 = ClassNullableUnderlayingInline(ClassInt(1))
    konst x1_2 = ClassNullableUnderlayingInline(ClassInt(1))
    konst x1_3 = ClassNullableUnderlayingInline(i1_1)
    konst x1_4 = ClassNullableUnderlayingInline(i1_2!!)

    // CHECK_NOT_CALLED_IN_SCOPE: scope=testNullableUnderlyingInlineClass$caseJsEq function=equals
    // CHECK_NEW_COUNT: function=testNullableUnderlyingInlineClass$caseJsEq count=0
    fun caseJsEq() {
        assertTrue(x_null == x_null)
        assertTrue(x1_1 == x1_2)
        assertTrue(x0 == ClassNullableUnderlayingInline(ClassInt(0)))
        assertTrue(x1_1 == x1_3)
        assertTrue(x1_4 == x1_3)
        assertTrue(x1_4 == x1_2)
        assertTrue(x_null == ClassNullableUnderlayingInline(null))
        assertTrue(ClassNullableUnderlayingInline(null) == x_null)

        assertFalse(x_null == x0)
        assertFalse(x0 == x_null)
        assertFalse(x1_1 == x_null)
        assertFalse(ClassNullableUnderlayingInline(null) == x1_4)
    }
    caseJsEq()

    // CHECK_CALLED_IN_SCOPE: scope=testNullableUnderlyingInlineClass$caseEquals function=equals
    // CHECK_NEW_COUNT: function=testNullableUnderlyingInlineClass$caseEquals count=3
    fun caseEquals() {
        assertTrue(ClassNullableUnderlayingInline(i1_1) == ClassNullableUnderlayingInline(i1_2!!))
    }
    caseEquals()
}

// CHECK_CALLED_IN_SCOPE: scope=testInlineClassWithInterface function=equals
// CHECK_NEW_COUNT: function=testInlineClassWithInterface count=14
fun testInlineClassWithInterface() {
    konst xi_1_1: InterfaceForInlineClass = ClassIntWithInterface(1)
    konst xi_1_2: InterfaceForInlineClass = ClassIntWithInterface(1)
    konst x_1 = ClassIntWithInterface(1)
    konst x_2 = ClassIntWithInterface(2)

    konst yi_foo: InterfaceForInlineClass = ClassStringWithInterface("foo")
    konst y_foo = ClassStringWithInterface("foo")
    konst y_bar = ClassStringWithInterface("bar")

    assertTrue(xi_1_1 == x_1)
    assertTrue(xi_1_1 == xi_1_2)
    assertTrue(yi_foo == y_foo)
    assertTrue(y_foo == yi_foo)

    assertTrue(xi_1_1 as ClassIntWithInterface == x_1)
    assertTrue(x_1 == xi_1_1 as ClassIntWithInterface)
    assertTrue(xi_1_2 as ClassIntWithInterface == xi_1_1 as ClassIntWithInterface)
    assertTrue(xi_1_2 as? ClassIntWithInterface == xi_1_1 as? ClassIntWithInterface)

    assertFalse(xi_1_1 == x_2)
    assertFalse(yi_foo == y_bar)
    assertFalse(xi_1_2 == yi_foo)
}

// CHECK_CALLED_IN_SCOPE: scope=testCompareDifferentInstancesInSmartCast function=equals
// CHECK_NEW_COUNT: function=testCompareDifferentInstancesInSmartCast count=6
@Suppress("EQUALITY_NOT_APPLICABLE")
fun testCompareDifferentInstancesInSmartCast() {
    konst x1_1: Any = ClassInt(1)
    konst x1_2 = ClassInt(1)

    konst y1: Any = ClassString("1")
    konst y_foo: Any = ClassString("foo")

    if (x1_1 is ClassInt && y1 is ClassString) {
        assertTrue(x1_2 == x1_1)

        assertFalse(x1_1 == y1)
        assertFalse(x1_2 == y1)
    }
    if (x1_1 is ClassInt && y_foo is ClassString) {
        assertFalse(x1_1 == y_foo)
        assertFalse(x1_2 == y_foo)
        assertFalse(y1 == y_foo)
    }
}

fun testCompareDifferentInstncesInInlineTemplate() {
    inline fun <reified T, reified S> myEq(x: T, y: S) = x == y

    // CHECK_NOT_CALLED_IN_SCOPE: scope=testCompareDifferentInstncesInInlineTemplate$caseJsEq function=equals
    // CHECK_NEW_COUNT: function=testCompareDifferentInstncesInInlineTemplate$caseJsEq count=0
    fun caseJsEq() {
        assertTrue(myEq(ClassInt(1), ClassInt(1)))
        assertTrue(myEq(ClassString("foo"), ClassString("foo")))

        assertFalse(myEq(ClassInt(1), ClassInt(2)))
        assertFalse(myEq(ClassString("foo"), ClassString("bar")))
    }
    caseJsEq()

    // CHECK_CALLED_IN_SCOPE: scope=testCompareDifferentInstncesInInlineTemplate$caseEquals function=equals
    // CHECK_NEW_COUNT: function=testCompareDifferentInstncesInInlineTemplate$caseEquals count=4
    fun caseEquals() {
        assertFalse(myEq(ClassInt(1), ClassString("bar")))
        assertFalse(myEq(ClassInt(1), ClassString("1")))
    }
    caseEquals()
}

// CHECK_CALLED_IN_SCOPE: scope=testNothing function=equals
// CHECK_NEW_COUNT: function=testNothing count=0
fun testNothing() {
    konst x_undefined_1 = ClassNothing(undefined)
    konst x_undefined_2 = ClassNothing(undefined)
    konst x_null_1 = ClassNothing(null)
    konst x_null_2 = ClassNothing(null)

    assertTrue(x_undefined_1 == x_undefined_1)
    assertTrue(x_undefined_1 == x_undefined_2)
    assertTrue(x_undefined_1 == ClassNothing(undefined))

    assertTrue(x_null_1 == x_null_1)
    assertTrue(x_null_1 == x_null_2)
    assertTrue(x_null_1 == ClassNothing(null))

    assertTrue(x_undefined_1 == x_null_1)
    assertTrue(x_null_1 == x_undefined_1)

    assertTrue(x_undefined_1 == ClassNothing(null))
    assertTrue(ClassNothing(null) == x_undefined_1)

    assertTrue(ClassNothing(undefined) == x_null_1)
    assertTrue(x_null_1 == ClassNothing(undefined))
}

// CHECK_CALLED_IN_SCOPE: scope=testNullableNothing function=equals
// CHECK_NEW_COUNT: function=testNullableNothing count=2
fun testNullableNothing() {
    konst x_nothing_undefined: ClassNothing? = ClassNothing(undefined)
    konst x_nothing_null: ClassNothing? = ClassNothing(null)
    konst x_null: ClassNothing? = null

    assertTrue(x_nothing_undefined == x_nothing_null)
    assertTrue(x_null == null)
    assertTrue(x_null == undefined)

    assertTrue(null == x_null)
    assertTrue(undefined == x_null)

    assertFalse(undefined == x_nothing_null)
    assertFalse(x_nothing_null == undefined)

    assertFalse(undefined == x_nothing_undefined)
    assertFalse(x_nothing_undefined == undefined)

    assertFalse(x_null == x_nothing_null)
    assertFalse(x_null == x_nothing_undefined)
    assertFalse(x_nothing_null == x_null)
    assertFalse(x_nothing_undefined == x_null)
}

fun box(): String {
    testBasicInt()
    testBasicString()
    testFunctionCall()
    testTypeErasing()
    testTypeErasingAndCast()
    testNullableInstances()
    testUnderlyingInline()
    testEqualsOverride()
    testNullableUnderlyingType()
    testUnderlyingWithEqualsOverride()
    testNullableUnderlyingInlineClass()
    testInlineClassWithInterface()
    testCompareDifferentInstancesInSmartCast()
    testCompareDifferentInstncesInInlineTemplate()
    testNothing()
    testNullableNothing()

    return "OK"
}
