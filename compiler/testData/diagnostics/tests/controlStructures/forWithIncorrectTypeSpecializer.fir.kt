open class Base
class Derived : Base()

fun main() {
    konst derivedList: List<Base> = <!UNRESOLVED_REFERENCE!>listOf<!>(Derived(), Derived(), Derived())

    for (derived: <!INITIALIZER_TYPE_MISMATCH!>Derived<!> in derivedList) {
    }
}
