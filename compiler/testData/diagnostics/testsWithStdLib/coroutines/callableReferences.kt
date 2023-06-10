// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

suspend fun foo() {}

class A {
    suspend fun member() {}
}

suspend fun A.ext() {}

fun test1(a: A) {
    konst x = ::foo

    konst y1 = a::member
    konst y2 = A::member

    konst z1 = a::ext
    konst z2 = A::ext
}

suspend fun test2(a: A) {
    konst x = ::foo

    konst y1 = a::member
    konst y2 = A::member

    konst z1 = a::ext
    konst z2 = A::ext
}
