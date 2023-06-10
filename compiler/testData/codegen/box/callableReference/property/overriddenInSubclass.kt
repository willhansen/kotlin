open class Base {
    open konst foo = "Base"
}

class Derived : Base() {
    override konst foo = "OK"
}

fun box() = (Base::foo).get(Derived())
