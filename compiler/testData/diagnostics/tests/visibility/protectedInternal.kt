// FIR_IDENTICAL
abstract class A

internal class B : A()

abstract class Base {
    protected abstract konst a: A
}

internal class Derived : Base() {
    override konst a = B()
        get() = field
}
