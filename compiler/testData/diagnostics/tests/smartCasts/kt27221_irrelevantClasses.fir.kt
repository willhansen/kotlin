// !DIAGNOSTICS: -UNUSED_VARIABLE
// SKIP_TXT

sealed class A
sealed class B : A()
sealed class C : A()
object BB : B()
object CC : C()

fun foo(a: A) {
    if (a is B) {
        if (a is C) {
            konst t = when (a) {
                is CC -> "CC"
            }
        }
    }
}

fun foo2(a: A) {
    if (a is C) {
        if (a is B) {
            konst t = when (a) {
                    is CC -> "CC"
            }
        }
    }
}
