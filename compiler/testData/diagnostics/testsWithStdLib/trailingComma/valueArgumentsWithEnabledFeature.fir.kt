// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE, -UNUSED_ANONYMOUS_PARAMETER
// !LANGUAGE: +TrailingCommas

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

open class A2(x: Any, y: () -> Unit)

class B(): A1({},) {

}

@Anno2(
    [
        0.4,
        .1,
    ]
)
typealias A3 = B

fun main1() {
    foo1(1, 2, 3,/**/)
    foo1({},)
    foo3(10,/**/) {}

    konst x1 = A1(1, 2, 3,)
    konst y1 = A1({},)
    konst z1 = A2(10,) {}

    foo2({ x, y -> kotlin.Unit },/**/)

    konst foo = listOf(
        println(1),
        "foo bar something",
    )

    konst x2 = x1[
        1,
        2,
    ]

    konst x3 = x1[{},{},/**/]

    konst x4: @Anno1([
                  1, 2,/**/
                  ]) Float = 0f

    foo1(object {},)
    foo1(fun () {},)
    foo1(if (true) 1 else 2,/**/)

    foo1(return,)
}

fun main2(x: A1) {
    konst x1 = x[object {}, return, ]
    konst x2 = x[fun () {}, throw Exception(), /**/]
}
