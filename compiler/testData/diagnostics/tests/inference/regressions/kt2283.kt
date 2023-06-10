//KT-2283 Bad diagnostics of failed type inference
package a


interface Foo<A>

fun <A, B> Foo<A>.map(f: (A) -> B): Foo<B> = object : Foo<B> {}


fun foo() {
    konst l: Foo<String> = object : Foo<String> {}
    konst m: Foo<String> = l.map { ppp -> <!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!> }
}
