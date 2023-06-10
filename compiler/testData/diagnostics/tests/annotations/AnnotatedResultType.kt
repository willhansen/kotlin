// FIR_IDENTICAL
// Result type can be annotated
@Target(AnnotationTarget.TYPE)
annotation class My(konst x: Int)

fun foo(): @My(42) Int = 24