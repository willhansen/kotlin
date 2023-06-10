// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_VARIABLE -UNUSED_VALUE
// SKIP_TXT

/*
 * TESTCASE NUMBER: 1
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-18532
 */
fun case_1() {
    konst x = In<Int>()
    konst y: In<*>
    y = x
    y.put(0)
    konst z: In<*> = x
    z.put(<!ARGUMENT_TYPE_MISMATCH!>0<!>)
}

/*
 * TESTCASE NUMBER: 2
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-18532
 */
fun case_2() {
    konst x = Inv<Int>()
    konst y: Inv<out Number>
    y = x
    y.put(0)
    konst z: Inv<out Number> = x
    z.put(<!ARGUMENT_TYPE_MISMATCH!>0<!>)
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst x = Inv<Number>()
    konst y: Inv<Number>
    y = x
    y.put(0)
    konst z: Inv<Number> = x
    z.put(0)
}

// TESTCASE NUMBER: 4
fun case_4() {
    konst x = In<Number>()
    konst y: In<Number>
    y = x
    y.put(0)
    konst z: In<Number> = x
    z.put(0)
}

/*
 * TESTCASE NUMBER: 5
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-18532
 */
fun case_5() {
    konst x = Inv<Int>()
    var y: Inv<out Number> = Inv<Int>()
    y = x
    y.put(0)
    konst z: Inv<out Number> = x
    z.put(<!ARGUMENT_TYPE_MISMATCH!>0<!>)
}

/*
 * TESTCASE NUMBER: 6
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-18532
 */
fun case_6() {
    konst x = Inv<Int>()
    var y: Inv<out Number> = Inv<Int>()
    if (true)
        y = x
    y.put(<!ARGUMENT_TYPE_MISMATCH!>0<!>)
    konst z: Inv<out Number> = x
    z.put(<!ARGUMENT_TYPE_MISMATCH!>0<!>)
}
