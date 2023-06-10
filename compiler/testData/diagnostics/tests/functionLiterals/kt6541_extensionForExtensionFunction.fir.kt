// !DIAGNOSTICS: -UNUSED_PARAMETER

interface Foo
fun (Foo.() -> Unit).invoke(b : Foo.() -> Unit)  {}

object Z {
    infix fun add(b : Foo.() -> Unit) : Z = Z
}

konst t2 = Z add <!ARGUMENT_TYPE_MISMATCH!>{ } <!TOO_MANY_ARGUMENTS!>{ }<!><!>
