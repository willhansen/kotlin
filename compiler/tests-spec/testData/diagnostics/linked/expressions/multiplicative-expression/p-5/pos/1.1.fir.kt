// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1
class Case1(var a: Int) {
    operator fun times(o: Int): Any? { TODO() }
    operator fun times(o: Case1):Any? { TODO() }
    operator fun div(o: Int): Any? { TODO() }
    operator fun div(o: Case1): Any? { TODO() }
    operator fun rem(o: Int): Any? { TODO() }
    operator fun rem(o: Case1): Any? { TODO() }
}

fun case1() {
    konst a = Case1(1) * 1
    konst b = Case1(1) * Case1( 1)
    konst c = Case1(1) / 1
    konst d = Case1(1) / Case1( 1)
    konst e = Case1(1) % 1
    konst f = Case1(1) % Case1( 1)

    a checkType { check<Any?>() }
    b checkType { check<Any?>() }
    c checkType { check<Any?>() }
    d checkType { check<Any?>() }
    e checkType { check<Any?>() }
    f checkType { check<Any?>() }
}

// TESTCASE NUMBER: 2
class Case2(var a: Int) {
    operator fun times(o: Int): Nothing? { TODO() }
    operator fun times(o: Case2):Nothing? { TODO() }
    operator fun div(o: Int): Nothing? { TODO() }
    operator fun div(o: Case2): Nothing? { TODO() }
    operator fun rem(o: Int): Nothing? { TODO() }
    operator fun rem(o: Case2): Nothing? { TODO() }
}

fun case2() {
    konst a = Case2(1) * 1
    konst b = Case2(1) * Case2( 1)
    konst c = Case2(1) / 1
    konst d = Case2(1) / Case2( 1)
    konst e = Case2(1) % 1
    konst f = Case2(1) % Case2( 1)

    a checkType { check<Nothing?>() }
    b checkType { check<Nothing?>() }
    c checkType { check<Nothing?>() }
    d checkType { check<Nothing?>() }
    e checkType { check<Nothing?>() }
    f checkType { check<Nothing?>() }
}
