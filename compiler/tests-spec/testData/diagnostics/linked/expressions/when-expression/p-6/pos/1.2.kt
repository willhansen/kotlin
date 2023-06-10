// LANGUAGE: +WarnAboutNonExhaustiveWhenOnAlgebraicTypes
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: 'When' with bound konstue and type test condition (invert type checking operator).
 * HELPERS: classes, sealedClasses, objects
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: SealedClass) = when (konstue_1) {
    !is SealedChild1 -> {}
    !is SealedChild2 -> {}
    !is SealedChild3 -> {}
}

/*
 * TESTCASE NUMBER: 2
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-22996
 */
fun case_2(konstue_1: SealedClass?): String = when (konstue_1) {
    !is SealedChild2 -> "" // including null
    <!USELESS_IS_CHECK!>is SealedChild2<!> -> ""
    null -> "" // redundant
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: SealedClass?): String = when (konstue_1) {
    !is SealedChild2? -> "" // null isn't included
    is SealedChild2 -> ""
    null -> ""
}

/*
 * TESTCASE NUMBER: 4
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-22996
 */
fun case_4(konstue_1: SealedClass?) {
    when (konstue_1) {
        !is SealedChild2 -> {} // including null
        <!USELESS_IS_CHECK!>is SealedChild2?<!> -> {} // redundant nullable type check
        else -> {}
    }
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Any): String {
    when (konstue_1) {
        is EmptyObject -> return ""
        !is ClassWithCompanionObject.Companion -> return ""
    }

    return ""
}
