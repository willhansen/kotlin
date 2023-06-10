interface A<out T> {
    konst konstue: T
}

interface B<out T : CharSequence> : A<T>

open class C(override konst konstue: String) : B<CharSequence>

interface X {
    konst konstue: CharSequence
}

class Y(konstue: String) : C(konstue), X

fun box(): String =
    (Y("OK") as X).konstue.toString()
