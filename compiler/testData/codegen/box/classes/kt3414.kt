interface A {
    fun foo(): Int
}

interface B {
    fun foo(): Int
}

class Z(konst a: A) : A by a, B

fun box(): String {
    konst s = Z(object : A {
        override fun foo(): Int {
            return 1;
        }
    });
    return if (s.foo() == 1) "OK" else "fail"
}