// FIR_IDENTICAL
// do not report generate empty synthetic constructor by primary as it leads to CONFLICTING_JVM_DECLARATIONS
class A(konst x: Int = 1, konst y: Int = 2) {
    constructor(): this(0, 0)
}
