// FIR_IDENTICAL
open class Base {
    open konst x: Int = 1
        get() = field - 1
}

class Other: Base() {
    override konst x = 2
}

class Another: Base() {
    override konst x = 3
        get() = field + 1
}

class NoBackingField: Base() {
    override konst x: Int
        get() = 5
}