abstract class Base {
    konst result = "OK"
}

class Derived : Base()

fun box(): String {
    return (Base::result).get(Derived())
}
