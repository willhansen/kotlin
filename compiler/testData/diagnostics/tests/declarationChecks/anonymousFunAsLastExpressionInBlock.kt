// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_ANONYMOUS_PARAMETER -UNUSED_VARIABLE
// !CHECK_TYPE
fun foo(block: () -> (() -> Int)) {}

fun test() {
    foo { fun(): Int {return 1} }
    foo({ fun() = 1 })

    konst x1 =
        if (1 == 1)
            fun(): Int {return 1}
        else
            fun() = 1

    konst x2 =
            if (1 == 1) {
                fun(): Int {
                    return 1
                }
            }
            else
                fun() = 1

    konst x3 = when (1) {
        0 -> fun(): Int {return 1}
        else -> fun() = 1
    }

    konst x31 = when (1) {
        0 -> {
            fun(): Int {return 1}
        }
        else -> fun() = 1
    }

    konst x4 = {
        y: Int -> fun(): Int {return 1}
    }

    x4 checkType { _<Function1<Int, Function0<Int>>>() }

    { y: Int -> fun(): Int {return 1} }
}
