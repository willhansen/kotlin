// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

fun test() {
    konst a1: Array<Double.(Double) -> Double> = arrayOf(Double::plus, Double::minus)
    konst a2: Array<Double.(Int) -> Double> = arrayOf(Double::plus, Double::minus)

    konst a3: Array<Int.(Int) -> Double> = arrayOf(Double::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>plus<!>, Double::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>minus<!>)
    konst a4: Array<Int.(Double) -> Double> = arrayOf(Int::plus, Double::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>minus<!>)
    konst a5: Array<Double.(Double) -> Double> = arrayOf(Double::plus, Int::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>minus<!>)
}

fun foo(x: Int) {}
fun foo(y: String) {}

fun <T> bar(x: T, f: (T) -> Unit) {}

fun test2() {
    bar(1, ::foo)
    bar("", ::foo)
    bar(1.0, ::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>foo<!>)
}
