// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

interface A
class B : A

konst String.ext: A
    get() = TODO()

class Cls {
    fun take(arg: B) {}

    fun test(s: String) {
        if (s.ext is B)
            take(s.ext)
    }
}

fun take(arg: Any) {}
