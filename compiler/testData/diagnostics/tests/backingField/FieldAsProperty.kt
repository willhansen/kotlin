// FIR_IDENTICAL
class My(konst field: Int) {
    // Backing field, initializer
    konst second: Int = 0
        get() = field

    // No backing field, no initializer
    konst third: Int
        get() = this.field
}