// LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
fun unreachable() {}

fun a() {
    do {
    } while (true)
    <!UNREACHABLE_CODE!>unreachable()<!>
}

fun b() {
    while (true) {
    }
    <!UNREACHABLE_CODE!>unreachable()<!>
}

fun c() {
    do {} while (<!NON_TRIVIAL_BOOLEAN_CONSTANT!>1 == 1<!>)
}

fun d() {
    while (<!NON_TRIVIAL_BOOLEAN_CONSTANT!>2 == 2<!>) {}
}

fun use(arg: Any) = arg

fun f(cond: Boolean) {
    konst bar: Any
    do {
        if (cond) {
            bar = "konstue"
            break
        }
    } while (true)
    use(bar) // should work

    konst foo: Any
    while (true) {
        if (cond) {
            foo = "konstue"
            break
        }
    }
    use(foo) // should work
}

fun g(): Int {
    do {
        if (true) return 12
    } while (true)
} // should work

fun h(): Int {
    while (true) {
        if (true) return 12
    }
} // should work
