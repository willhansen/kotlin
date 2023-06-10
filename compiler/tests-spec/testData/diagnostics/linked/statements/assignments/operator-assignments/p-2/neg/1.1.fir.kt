// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

class B(var a: Int) {
    operator fun plus(konstue: Int): B {
        a= a + konstue
        return this
    }
    operator fun plusAssign(konstue: Int): Unit {
        a= a + konstue
    }
}

// TESTCASE NUMBER: 1
fun case1() {
    var b = B(1)
    b <!ASSIGN_OPERATOR_AMBIGUITY!>+=<!> 1
}
