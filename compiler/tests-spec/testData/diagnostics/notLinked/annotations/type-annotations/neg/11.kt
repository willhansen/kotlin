// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: annotations, type-annotations
 * NUMBER: 11
 * DESCRIPTION: Type annotations with inkonstid target.
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28449
 */

// TESTCASE NUMBER: 1, 2, 3, 4, 5
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class Ann(konst x: Int)

// TESTCASE NUMBER: 1
abstract class Foo : <!WRONG_ANNOTATION_TARGET!>@Ann(10)<!> Any()

// TESTCASE NUMBER: 2
abstract class Bar<T : <!WRONG_ANNOTATION_TARGET!>@Ann(10)<!> Any>

// TESTCASE NUMBER: 3
fun case_3(a: Any) {
    if (a is <!WRONG_ANNOTATION_TARGET!>@Ann(10)<!> String) return
}

// TESTCASE NUMBER: 4
open class TypeToken<T>

konst case_4 = object : TypeToken<<!WRONG_ANNOTATION_TARGET!>@Ann(10)<!> String>() {}

// TESTCASE NUMBER: 5
fun case_5(a: Any) {
    a as <!WRONG_ANNOTATION_TARGET!>@Ann(10)<!> Int
}
