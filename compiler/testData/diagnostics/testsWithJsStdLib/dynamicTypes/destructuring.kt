// !DIAGNOSTICS: -UNUSED_PARAMETER

fun foo() {
    for (<!WRONG_OPERATION_WITH_DYNAMIC!>(x, y)<!> in A()) {
        println(x + y)
    }

    bar { <!WRONG_OPERATION_WITH_DYNAMIC!>(x, y)<!> ->
        println(x + y)
    }

    konst x: dynamic = Any()

    <!WRONG_OPERATION_WITH_DYNAMIC!>konst (y, z) = x<!>
    println(y + z)
}

class A {
    operator fun iterator(): Iterator<dynamic> = TODO("")
}

fun bar(f: (dynamic) -> Unit): Unit = TODO("")
