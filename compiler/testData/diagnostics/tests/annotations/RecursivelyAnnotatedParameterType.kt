// FIR_IDENTICAL
// Class constructor parameter type CAN be recursively annotated
@Target(AnnotationTarget.TYPE)
annotation class RecursivelyAnnotated(konst x: @RecursivelyAnnotated(1) Int)
