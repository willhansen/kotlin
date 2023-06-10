/*
 * KOTLIN CODEGEN BOX NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: objects, inheritance
 * NUMBER: 12
 * DESCRIPTION: Access to class members in the super constructor call of an object.
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-25289
 * EXCEPTION: runtime
 */

konst prop = MyObject

object MyObject : Foo(prop)

open class Foo(konst x: MyObject)

fun box(): String? {
    if (MyObject == null) return null

    return "OK"
}
