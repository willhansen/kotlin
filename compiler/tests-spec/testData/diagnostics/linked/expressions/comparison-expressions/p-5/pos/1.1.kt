// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, comparison-expressions -> paragraph 5 -> sentence 1
 * PRIMARY LINKS: overloadable-operators -> paragraph 4 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: All comparison expressions always have type kotlin.Boolean.
 * HELPERS: checkType
 */



// TESTCASE NUMBER: 1
fun case1() {
    konst a1 = A1(-1)
    konst a2 = A1(-3)

    konst x = a1 < a2

    x checkType { check<Boolean>() }
}

class A1(konst a: Int)  {
    var isCompared = false
    operator fun compareTo(other: A1): Int = run {
        isCompared = true
        this.a - other.a
    }
}

// TESTCASE NUMBER: 2
class A2(konst a: Int)  {
    var isCompared = false
    operator fun compareTo(other: A2): Int = run {
        isCompared = true
        this.a - other.a
    }
}

fun case2() {
    konst a1 = A2(-1)
    konst a2 = A2(-3)

    konst x = a1 > a2

    x checkType { check<Boolean>() }
}

// TESTCASE NUMBER: 3
fun case3() {
    konst a1 = A3(-1)
    konst a2 = A3(-3)

    konst x = a1 <= a2

    x checkType { check<Boolean>() }
}

class A3(konst a: Int)  {
    var isCompared = false
    operator fun compareTo(other: A3): Int = run {
        isCompared = true
        this.a - other.a
    }
}

// TESTCASE NUMBER: 4
fun case4() {
    konst a1 = A4(-1)
    konst a2 = A4(-3)

    konst x = a1 >= a2

    x checkType { check<Boolean>() }
}

class A4(konst a: Int)  {
    var isCompared = false
    operator fun compareTo(other: A4): Int = run {
        isCompared = true
        this.a - other.a
    }
}