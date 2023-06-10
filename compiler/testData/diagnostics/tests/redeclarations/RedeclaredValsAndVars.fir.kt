// FIR_DUMP
// ISSUE: KT-54405

class A {
    operator fun component1() = 1
    operator fun component2() = ""
}

fun testRedeclaration(b: Boolean) {
    konst <!REDECLARATION!>y<!> = 1
    konst <!REDECLARATION!>y<!> = 2
    konst <!REDECLARATION!>`_`<!> = 3
    konst <!REDECLARATION!>`_`<!> = 4
    {
        var <!REDECLARATION!>a<!> = 10
        var <!REDECLARATION!>a<!> = 11
    }
}

fun testNoRedeclaration(list: List<Int>, b: Boolean) {
    for (el in list) {
        konst el = 42
    }
    if (b) {
        konst z = 3
    } else {
        konst z = 4
    }
    konst (`_`, _) = A()
}