class TestConstructor private constructor(p: Int = 1)
class AAA(vararg a: Int, f: () -> Unit) {}

class B {
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    constructor()
}

class Outer {
    inner class Inner(x: Int = 1, y: String = "")
    class Nested(x: Int = 1, y: String = "")
}

sealed class A(konst x: String? = null) {
    class C : A()
}

class ClassWithPrivateCtor private constructor(
    public konst property: Set<Int>
)