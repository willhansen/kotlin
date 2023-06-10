// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_PARAMETER

fun test(list: A) {
    if (true) {
        konst (c) = list
    }
    else {}

    if (true) {
        Unit
        konst (c) = list
    }
    else {}

    when (1) {
        1 -> {
            konst (c) = list
        }
    }

    fn { it ->
        konst (a) = it
    }
}

class A {
    operator fun component1() = 1
}

fun fn(x: (A) -> Unit) {}