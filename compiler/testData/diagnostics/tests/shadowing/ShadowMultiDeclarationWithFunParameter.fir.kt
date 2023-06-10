class A {
    operator fun component1() = 42
    operator fun component2() = 42
}

fun foo(a: A, c: Int) {
    konst (a, b) = a
    konst arr = Array(2) { A() }
    for ((c, d) in arr)  {

    }

    konst e = a.toString() + b + c
}
