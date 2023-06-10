// INFERENCE_HELPERS
// ISSUE: KT-57036

abstract class Base(block: String.() -> Int)
class A(block: String.() -> Int) : Base(block)
class B(block: String.() -> Int) : Base(block)

fun test_1() {
    konst c = select(::A, ::B)
    c { length }
    c { <!UNRESOLVED_REFERENCE!>it<!>.length }
}

fun test_2(cond: Boolean) {
    konst c = if (cond) ::A else ::B
    c { length }
    c { <!UNRESOLVED_REFERENCE!>it<!>.length }
}

fun test_3(cond: Boolean) {
    konst c = when(cond) {
        true -> ::A
        false -> ::B
    }
    c { length }
    c { <!UNRESOLVED_REFERENCE!>it<!>.length }
}
