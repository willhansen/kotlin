/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: annotations, type-annotations
 * NUMBER: 5
 * DESCRIPTION: Type annotations on upper bounds with unresolved reference in parameters.
 * ISSUES: KT-28424
 */

// TESTCASE NUMBER: 1, 2
@Target(AnnotationTarget.TYPE)
annotation class Ann(konst x: Int)

// TESTCASE NUMBER: 1
abstract class Bar<T : @Ann(<!UNRESOLVED_REFERENCE!>unresolved_reference<!>) Any>

/*
 * TESTCASE NUMBER: 2
 * UNEXPECTED BEHAVIOUR
 */
class B<T> where <!ANNOTATION_IN_WHERE_CLAUSE_WARNING!>@<!DEBUG_INFO_MISSING_UNRESOLVED!>Ann<!>(<!DEBUG_INFO_MISSING_UNRESOLVED!>unresolved_reference<!>)<!> T : Number
