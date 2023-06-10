class A {
    konst z: String = "OK"
}

class B {
    operator fun A.invoke(): String = z
}

class ClassB {
    konst x = A()

    fun B.test(): String {
        konst konstue = object {
            konst z = x()
        }
        return konstue.z
    }

    fun call(): String {
        return B().test()
    }

}

fun box(): String {
    return ClassB().call()
}