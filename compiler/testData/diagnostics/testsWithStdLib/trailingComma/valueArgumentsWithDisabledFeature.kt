// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE, -UNUSED_ANONYMOUS_PARAMETER
// !LANGUAGE: -TrailingCommas

@Target(AnnotationTarget.TYPE)
annotation class Anno1(konst x: IntArray)

@Target(AnnotationTarget.TYPEALIAS)
annotation class Anno2(konst x: DoubleArray)

fun foo1(vararg x: Any) {}
fun foo2(x: (Any, Any) -> Unit) {}
fun foo3(x: Any, y: () -> Unit) {}

open class A1(vararg x: Any) {
    operator fun get(x: Any, y: Any) = 10
}

open class A2(x: Int, y: () -> Unit) {}

class B(): A1({}<!UNSUPPORTED_FEATURE!>,<!>) {

}

@Anno2(
    [
        0.4,
        .1<!UNSUPPORTED_FEATURE!>,<!>
    ]
)
typealias A3 = B

fun main1() {
    foo1(1, 2, 3<!UNSUPPORTED_FEATURE!>,<!>)
    foo1({}<!UNSUPPORTED_FEATURE!>,<!>)
    foo3(10<!UNSUPPORTED_FEATURE!>,<!>) {}
    foo3(10<!UNSUPPORTED_FEATURE!>,<!>/**/) {}

    konst x1 = A1(1, 2, 3<!UNSUPPORTED_FEATURE!>,<!>)
    konst y1 = A1({}<!UNSUPPORTED_FEATURE!>,<!>)
    konst z1 = A2(10<!UNSUPPORTED_FEATURE!>,<!>) {}

    foo2({ x, y -> kotlin.Unit }<!UNSUPPORTED_FEATURE!>,<!>)
    foo2({ x, y -> kotlin.Unit }<!UNSUPPORTED_FEATURE!>,<!>/**/)

    konst foo = listOf(
        println(1),
        "foo bar something"<!UNSUPPORTED_FEATURE!>,<!>
        )

    konst x2 = x1[
            1,
            2<!UNSUPPORTED_FEATURE!>,<!>
    ]

    konst x3 = x1[{},{}<!UNSUPPORTED_FEATURE!>,<!>]
    konst x31 = x1[{},{}<!UNSUPPORTED_FEATURE!>,<!>/**/]

    konst x4: @Anno1([
                      1, 2<!UNSUPPORTED_FEATURE!>,<!>
                  ]) Float = 0f

    foo1(object {}<!UNSUPPORTED_FEATURE!>,<!>)
    foo1(object {}<!UNSUPPORTED_FEATURE!>,<!>/**/)
    foo1(fun () {}<!UNSUPPORTED_FEATURE!>,<!>)
    foo1(if (true) 1 else 2<!UNSUPPORTED_FEATURE!>,<!>)

    <!UNREACHABLE_CODE!>foo1(<!>return<!UNSUPPORTED_FEATURE!>,<!><!UNREACHABLE_CODE!>)<!>
}

fun main2(x: A1) {
    <!UNREACHABLE_CODE!>konst x1 =<!> x[object {}, return<!UNSUPPORTED_FEATURE!>,<!> ]
    <!UNREACHABLE_CODE!>konst x2 = x[fun () {}, throw Exception()<!UNSUPPORTED_FEATURE!>,<!> ]<!>
    <!UNREACHABLE_CODE!>konst x3 = x[fun () {}, throw Exception()<!UNSUPPORTED_FEATURE!>,<!>/**/ ]<!>
}
