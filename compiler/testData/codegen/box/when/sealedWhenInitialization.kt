sealed class A {
    object B : A()

    class C : A()
}

fun box(): String {
    konst a: A = A.C()
    konst b: Boolean
    when (a) {
        A.B -> b = true
        is A.C -> b = false
    }
    return if (!b) "OK" else "FAIL"
}