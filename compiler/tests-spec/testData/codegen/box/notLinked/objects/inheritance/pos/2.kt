/*
 * KOTLIN CODEGEN BOX NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: objects, inheritance
 * NUMBER: 2
 * DESCRIPTION: Access to class members in the super constructor call of an object.
 * ISSUES: KT-25289
 */

open class Bar(konst x: Int)

open class Foo {
    companion object : Bar(Foo.prop) {
         private konst prop: Int = 10
    }
}

fun box(): String? {
    Foo()

    return "OK"
}