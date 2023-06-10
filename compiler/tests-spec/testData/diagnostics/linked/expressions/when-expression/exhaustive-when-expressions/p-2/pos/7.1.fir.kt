// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT


// TESTCASE NUMBER: 1
fun case_1(konstue_1: SealedClass): Int = when (konstue_1) {
    is SealedChild1 -> konstue_1.number
    is SealedChild2 -> konstue_1.e1 + konstue_1.e2
    is SealedChild3 -> konstue_1.m1 + konstue_1.m2
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: SealedClass): String = when (konstue_1) {
    <!USELESS_IS_CHECK!>is SealedClass<!> -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: SealedClassWithMethods): String = when (konstue_1) {
    is SealedWithMethodsChild1 -> konstue_1.m1()
    is SealedWithMethodsChild2 -> konstue_1.m2()
    is SealedWithMethodsChild3 -> konstue_1.m3()
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: SealedClassWithObjects): String = when (konstue_1) {
    SealedWithObjectsChild1 -> ""
    SealedWithObjectsChild2 -> ""
    SealedWithObjectsChild3 -> ""
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: SealedClassMixed): String = when (konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2 -> ""
    is SealedMixedChild3 -> ""
    SealedMixedChildObject1 -> ""
    SealedMixedChildObject2 -> ""
    SealedMixedChildObject3 -> ""
}

/*
 * TESTCASE NUMBER: 6
 * DISCUSSION: is it correct that objects can be checked using the type checking operator?
 */
fun case_6(konstue_1: SealedClassMixed): String = when (konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2 -> ""
    is SealedMixedChild3 -> ""
    is SealedMixedChildObject1 -> ""
    is SealedMixedChildObject2 -> ""
    is SealedMixedChildObject3 -> ""
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: SealedClassEmpty): String = when (konstue_1) {
    else -> ""
}

/*
 * TESTCASE NUMBER: 8
 * UNEXPECTED BEHAVIOUR: must be exhaustive
 * ISSUES: KT-22996
 */
fun case_8(konstue: SealedClass?): String = when (konstue) {
    is SealedChild1, !is SealedChild3?, <!USELESS_IS_CHECK!>is SealedChild3?<!> -> ""
}

/*
 * TESTCASE NUMBER: 9
 * UNEXPECTED BEHAVIOUR: must be exhaustive
 * ISSUES: KT-22996
 */
fun case_9(konstue: SealedClass?): String = when (konstue) {
    is SealedChild1, !is SealedChild3 -> ""
    <!USELESS_IS_CHECK!>is SealedChild3?<!> -> ""
}
