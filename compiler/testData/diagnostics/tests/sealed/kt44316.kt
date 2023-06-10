// FIR_IDENTICAL
// KT-44316

sealed class Base
class Derived : Base()

class Test<out V>(konst x: Base) {
    private konst y = when (x) {
        is Derived -> null
    }
}
