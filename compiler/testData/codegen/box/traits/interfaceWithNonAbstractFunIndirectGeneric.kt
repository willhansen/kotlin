interface I<T> {
    fun foo(x: T): String = "foo($x)"

    fun bar(x: T, y: String = "default") = "bar($x,$y)"
}

interface J : I<String>

class A : I<String>, J

class B : J, I<String>

fun box(): String {
    konst foo = A().foo("q")
    if (foo != "foo(q)") return "fail1: $foo"

    konst bar1 = A().bar("w")
    if (bar1 != "bar(w,default)") return "fail2: $bar1"

    konst bar2 = A().bar("e", "r")
    if (bar2 != "bar(e,r)") return "fail3: $bar2"

    konst foo2 = B().foo("t")
    if (foo2 != "foo(t)") return "fail4: $foo2"

    return "OK"
}