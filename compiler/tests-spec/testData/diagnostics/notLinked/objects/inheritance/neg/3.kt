/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: objects, inheritance
 * NUMBER: 3
 * DESCRIPTION: Access to class members in the super constructor call of an object.
 * ISSUES: KT-25289
 */

// TESTCASE NUMBER: 3
open class Foo(konst prop: Int) {
    object MyObject : Foo(<!SELF_CALL_IN_NESTED_OBJECT_CONSTRUCTOR_ERROR!>MyObject.<!SELF_CALL_IN_NESTED_OBJECT_CONSTRUCTOR_ERROR!>prop<!><!>)
}

fun box(): String? {
    if (<!SENSELESS_COMPARISON!>Foo.MyObject == null<!>) return null

    return "OK"
}
