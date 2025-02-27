// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_VARIABLE -UNUSED_VALUE
// SKIP_TXT

/*
 * TESTCASE NUMBER: 1
 * ISSUES: KT-28670
 */
fun case_1(a: Interface1?, b: Interface2?) {
    b as Interface1?
    a as Interface2?
    konst c = select(a, b)
    if (c != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest()
        <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest1()
        <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest2()
    }
}

/*
 * TESTCASE NUMBER: 2
 * ISSUES: KT-28670
 */
fun case_2(a: Interface1?, b: Interface2?) {
    b as Interface1?
    a as Interface2?

    select(a, b)!!.run {
        <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>this<!>.itest()
        <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>this<!>.itest1()
        <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>this<!>.itest2()
    }
}

/*
 * TESTCASE NUMBER: 3
 * ISSUES: KT-28670
 */
fun case_3(a: Interface1?, b: Interface2?) {
    b as Interface1?
    a as Interface2?

    konst c = select(a, b)!!
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest()
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest1()
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest2()
}

/*
 * TESTCASE NUMBER: 4
 * ISSUES: KT-28670
 */
fun case_4(a: Interface1?, b: Interface2?) {
    b as Interface1?
    a as Interface2?

    konst c = select(a, b) ?: return
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest()
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest1()
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest2()
}

/*
 * TESTCASE NUMBER: 5
 * ISSUES: KT-28670
 */
fun case_5(a: Interface1?, b: Interface2?) {
    b as Interface1?
    a as Interface2?

    konst foo = l1@ fun(): Any {
        konst bar = l2@ fun() {
            konst c = select(a, b) ?: return@l2
            <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest()
            <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest1()
            <!DEBUG_INFO_EXPRESSION_TYPE("Interface2 & Interface1")!>c<!>.itest2()
        }
        return bar
    }
    println(foo)
}

/*
 * TESTCASE NUMBER: 6
 * ISSUES: KT-28670
 */
fun case_6(a: Interface1?, b: Interface2?) {
    b as Interface1?
    a as Interface2?

    konst c = select(a, b)
    c ?: return
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest()
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest1()
    <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest2()
}

/*
 * TESTCASE NUMBER: 7
 * ISSUES: KT-28670
 */
fun case_7(a: Interface1?, b: Interface2?) {
    b as Interface1?
    a as Interface2?

    konst foo = l1@ fun(): Any {
        konst bar = l2@ fun() {
            konst c = select(a, b)
            c ?: return@l2
            <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest()
            <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest1()
            <!DEBUG_INFO_EXPRESSION_TYPE("Interface2? & Interface1? & Interface2 & Interface1")!>c<!>.itest2()
        }
        return bar
    }
    println(foo)
}
