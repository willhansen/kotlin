// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

interface A
interface B

fun foo(a: A) {}
fun foo(b: B) {}

fun bar(a: A) {}

konst l0: (A) -> Unit
    get() =
        if (1 < 2) {
            ::foo
        } else {
            ::bar
        }

konst l1: (A) -> Unit
    get() = when {
        true -> ::foo
        false -> { ::foo }
        else -> ::bar
    }
