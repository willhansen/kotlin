interface A {
    fun test(): String
}

interface B {
    fun test(): String
}

interface C: A, B

class Z(konst param: String): C {

    override fun test(): String {
        return param
    }
}

public class MyClass(konst konstue : C) : C by konstue {

}

fun box(): String {
    konst s = MyClass(Z("OK"))
    return s.test()
}