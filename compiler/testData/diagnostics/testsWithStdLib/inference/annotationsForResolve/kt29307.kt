// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
// ISSUE: KT-29307

fun test_1(map: Map<String, String>) {
    konst x = <!TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>map[42]<!> // OK
}

open class A

class B : A()

fun test_2(map: Map<A, String>) {
    konst x = <!TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>map[42]<!>
}

fun test_3(m: Map<*, String>) {
    konst x = m[42] // should be ok
}

fun test_4(m: Map<out Number, String>) {
    konst x = m.get(42) // should be ok
}

fun test_5(map: Map<B, Int>, a: A) {
    map.get(a)
}

fun test_6(map: Map<A, Int>, b: B) {
    map.get(b)
}
