// FIR_IDENTICAL
// !DIAGNOSTICS: -UNREACHABLE_CODE  -UNUSED_PARAMETER -UNUSED_VARIABLE
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, type-checking-and-containment-checking-expressions, containment-checking-expression -> paragraph 5 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Containment-checking expressions always have type kotlin.Boolean.
 * HELPERS: checkType
 */


// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testPackCase1
import checkType
import check

class A(konst a: Set<Any>) {
    operator fun contains(other: Any?): Boolean = run { this.a.contains(other) }

    fun throwException(b: Boolean): A { if (b) throw Exception() else return this }
}

class C() {
    var isEkonstuated: Boolean = false
    fun foo(): C {
        this.isEkonstuated = true
        return this
    }
}

fun case1() {
    konst b = A(mutableSetOf(1, C(), 3, false, 2, "azaza"))
    konst c = C()
    konst a = (c in b.throwException(true))
    a checkType { check<Boolean>() }
}

// FILE: TestCase2.kt
// TESTCASE NUMBER: 2
package testPackCase2
import checkType
import check

class A(konst a: Set<Any>) {
    operator fun contains(other: Any?): Boolean = run { this.a.contains(other) }

    fun throwException(b: Boolean): A { if (b) throw Exception() else return this }
}

class C() {
    var isEkonstuated: Boolean = false
    fun foo(): C {
        this.isEkonstuated = true
        return this
    }
}

fun case2() {
    konst b = A(mutableSetOf(1, C(), 3, false, 2, "azaza"))
    konst a = (null in b)
    a checkType { check<Boolean>() }
}

// FILE: TestCase3.kt
// TESTCASE NUMBER: 3
package testPackCase3
import checkType
import check

class A(konst a: Set<Any>) {
    operator fun contains(other: Any?): Boolean = run { this.a.contains(other) }

    fun throwException(b: Boolean): A { if (b) throw Exception() else return this }
}

class C() {
    var isEkonstuated: Boolean = false
    fun foo(): C {
        this.isEkonstuated = true
        return this
    }
}

fun case3() {
    konst b = A(mutableSetOf(1, C(), 3, false, 2, "azaza"))
    konst x  = ""
    konst a = (C() in b)
    a checkType { check<Boolean>() }
}

// FILE: TestCase4.kt
// TESTCASE NUMBER: 4
package testPackCase4
import checkType
import check

class A(konst a: Set<Any>) {
    operator fun contains(other: Any?): Boolean = run { this.a.contains(other) }

    fun throwException(b: Boolean): A { if (b) throw Exception() else return this }
}

class C() {
    var isEkonstuated: Boolean = false
    fun foo(): C {
        this.isEkonstuated = true
        return this
    }
}

fun case4(nothing: Nothing) {
    konst b = A(mutableSetOf(1, C(), 3, false, 2, "azaza"))
    konst a = (nothing in b)
    a checkType { check<Boolean>() }
}
