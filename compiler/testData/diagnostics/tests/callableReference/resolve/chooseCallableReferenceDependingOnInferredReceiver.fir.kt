// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNUSED_EXPRESSION

class A {
    fun foo(i: A) {}

    fun baz(i: A) {}
}

class B {
    fun foo(s: B) {}
    fun foo(c: Char) {}

    fun baz(s: B) {}
}

fun <T> bar(f: (T) -> Unit): T = TODO()

fun test() {
    <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>myWith<!>(A()) {
        konst t1 = bar(::foo)
        t1

        konst t2 = bar(::baz)
        t2

        myWith(B()) {
            konst a: A = bar(::foo)
            konst b: B = bar(::foo)

            konst t3 = bar(::baz)
            t3

            bar(::<!OVERLOAD_RESOLUTION_AMBIGUITY!>foo<!>)
        }
    }
}

inline fun <T, R> myWith(receiver: T, block: T.() -> R): R = TODO()
