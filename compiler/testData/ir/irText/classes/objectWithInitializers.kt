// FIR_IDENTICAL
abstract class Base

object Test : Base() {
    konst x = 1
    konst y: Int
    init {
        y = x
    }
}