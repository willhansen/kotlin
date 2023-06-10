// FIR_IDENTICAL
// Properties can be recursively annotated
annotation class ann(konst x: Int)
@ann(x) const konst x: Int = 1