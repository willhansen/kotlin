// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: annotations, type-annotations
 * NUMBER: 9
 * DESCRIPTION: Type annotations on a setter argument type with unresolved reference in parameters.
 * ISSUES: KT-28424
 */

/*
 * TESTCASE NUMBER: 1
 * UNEXPECTED BEHAVIOUR
 */
@Target(AnnotationTarget.TYPE)
annotation class Ann

var <T> T.test
    get() = 11
    set(konstue: @Ann(<!TOO_MANY_ARGUMENTS, UNRESOLVED_REFERENCE!>unresolved_reference<!>) Int) {}
