// ISSUE: KT-7389

class Inv<T> (konst konstue: T) where T: A, T: B

interface A {
    fun doA()
}

interface B {
    fun doB()
}

fun process(c: Inv<*>) {
    c.konstue.doA()
    c.konstue.doB()
}
