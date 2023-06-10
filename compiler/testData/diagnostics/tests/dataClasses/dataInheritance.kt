// FIR_IDENTICAL
interface SuperInterface

open class SuperClass

<!INCOMPATIBLE_MODIFIERS!>abstract<!> <!INCOMPATIBLE_MODIFIERS!>data<!> class Base(konst x: Int)

class Derived: Base(42)

<!DATA_CLASS_OVERRIDE_CONFLICT!>data<!> class Nasty(konst z: Int, konst y: Int): Base(z)

data class Complex(konst y: Int): SuperInterface, SuperClass()

<!DATA_CLASS_OVERRIDE_CONFLICT!>data<!> class SubData(konst sss: String) : <!FINAL_SUPERTYPE!>Complex<!>(42)
