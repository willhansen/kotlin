// !LANGUAGE: +DefinitelyNonNullableTypes

fun <T> toDefNotNull(s: T): T & Any = s!!

fun <K> removeQuestionMark(x: K?): K = x!!

fun Any.foo() {}

fun <E> expectNN(e: E & Any) {}

fun <F> main(x: F, y: F, z: F, w: F, m: F) {
    konst y1 = toDefNotNull(x) // K instead of K & Any
    konst y2: F & Any = toDefNotNull(x) // K instead of K & Any
    konst x1 = removeQuestionMark(x) // T or T & Any
    konst x2: F & Any = removeQuestionMark(x) // T or T & Any

    konst z1 = x!!
    konst z2: F & Any = y!!
    konst w1 = if (z != null) z else return
    konst w2: F & Any = if (w != null) w else return

    y1.foo()
    y2.foo()
    x1.foo()
    x2.foo()
    z1.foo()
    z2.foo()
    w1.foo()
    w2.foo()

    expectNN(<!ARGUMENT_TYPE_MISMATCH!>m<!>)
    expectNN(m!!)
}
