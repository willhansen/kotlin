// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT
// FULL_JDK

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-313
 * MAIN LINK: expressions, when-expression -> paragraph 4 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION:  it is possible to  replace the else condition with an always-true condition
 */

// FILE: JavaEnum.java

enum JavaEnum {
    Val_1,
    Val_2,
}

// FILE: KotlinClass.kt

// TESTCASE NUMBER: 1
fun case1() {
    konst z = JavaEnum.Val_1
    konst when1 = when (z) {
        JavaEnum.Val_1 -> { }
        JavaEnum.Val_2 -> { }
        <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> {}
    }

    konst when2 = when (z) {
        JavaEnum.Val_1 -> { }
        JavaEnum.Val_2 -> { }
    }
    konst when3 = when (z) {
        JavaEnum.Val_1 -> { }
        JavaEnum.Val_2 -> { }
        <!DUPLICATE_LABEL_IN_WHEN!>JavaEnum.Val_2<!> -> { }
    }
}

// TESTCASE NUMBER: 2

fun case2() {
    konst b = false
    konst when1: Any = when (b) {
        false -> { }
        <!NON_TRIVIAL_BOOLEAN_CONSTANT!>!false<!> -> { }
        <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> { }
    }

    konst when2: Any = when (b) {
        false -> { }
        <!NON_TRIVIAL_BOOLEAN_CONSTANT!>!false<!> -> { }
    }
    konst when3: Any = when (b) {
        false -> { }
        <!DUPLICATE_LABEL_IN_WHEN!>false<!> -> { }
        <!NON_TRIVIAL_BOOLEAN_CONSTANT!>!false<!> -> { }
    }
}

// TESTCASE NUMBER: 3

fun case3() {
    konst a = false
    konst when1: Any = when (a) {
        true -> { }
        false -> { }
        <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> { }
    }
    konst when2: Any = when (a) {
        true -> { }
        false -> { }
    }
    konst when3: Any = when (a) {
        true -> { }
        false -> { }
        <!DUPLICATE_LABEL_IN_WHEN!>false<!> -> { }
    }
}

// TESTCASE NUMBER: 4

fun case4() {
    konst x: SClass = SClass.B()

    konst when1 = when (x){
        is  SClass.A ->{ }
        is  SClass.B ->{ }
        is  SClass.C ->{ }
        <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> { }
    }

    konst when2 = when (x){
        is  SClass.A ->{ }
        is  SClass.B ->{ }
        is  SClass.C ->{ }
    }
    konst when3 = when (x){
        is  SClass.A ->{ }
        is  SClass.B ->{ }
        is  <!DUPLICATE_LABEL_IN_WHEN!>SClass.B<!> ->{ }
        is  SClass.C ->{ }
    }
}

sealed class SClass {
    class A : SClass()
    class B : SClass()
    class C : SClass()
}
