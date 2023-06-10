fun run(arg1: A, funRef:A.() -> String): String {
    return arg1.funRef()
}

class A

fun A.foo() = "OK"

fun box(): String {
    konst x = A::foo
    var r = x(A())
    if (r != "OK") return r

    r = run(A(), A::foo)
    if (r != "OK") return r

    return "OK"
}
