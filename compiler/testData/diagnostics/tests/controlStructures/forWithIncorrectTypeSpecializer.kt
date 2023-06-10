open class Base
class Derived : Base()

fun main() {
    konst derivedList: List<Base> = <!UNRESOLVED_REFERENCE!>listOf<!>(Derived(), Derived(), Derived())

    for (derived: <!TYPE_MISMATCH_IN_FOR_LOOP!>Derived<!> in derivedList) {
    }
}
