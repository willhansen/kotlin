interface A
interface B

fun multiple(a: A) {}
fun multiple(b: B) {}

fun singleA(a: A) {}
fun singleB(a: B) {}

fun <T> foo(f: (T) -> Unit, g: (T) -> Unit): T = TODO()

fun test() {
    konst a1 = foo(::singleA, ::multiple)

    konst a2 = foo(::singleB, ::multiple)

    konst a3 = foo(::multiple, ::singleA)

    konst a4 = foo(::multiple, ::singleB)

    konst a5 = foo(::singleA, ::singleA)

    konst a6 = foo(::singleA, ::singleB)

    <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>foo<!>(::<!OVERLOAD_RESOLUTION_AMBIGUITY!>multiple<!>, ::<!OVERLOAD_RESOLUTION_AMBIGUITY!>multiple<!>)
}
