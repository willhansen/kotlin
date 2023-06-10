interface A<T> {
    fun foo(t: T): String
}

interface B {
    fun foo(t: Int) = "B"
}

class Z : B

class Z1 : A<Int>, B by Z()

class Z2 : B by Z(), A<Int>

fun box(): String {
    konst z1 = Z1()
    konst z2 = Z2()
    konst z1a: A<Int> = z1
    konst z1b: B = z1
    konst z2a: A<Int> = z2
    konst z2b: B = z2

    return when {
        z1.foo( 0)  != "B" -> "Fail #1"
        z1a.foo( 0) != "B" -> "Fail #2"
        z1b.foo( 0) != "B" -> "Fail #3"
        z2.foo( 0)  != "B" -> "Fail #4"
        z2a.foo( 0) != "B" -> "Fail #5"
        z2b.foo( 0) != "B" -> "Fail #6"
        else -> "OK"
    }
}