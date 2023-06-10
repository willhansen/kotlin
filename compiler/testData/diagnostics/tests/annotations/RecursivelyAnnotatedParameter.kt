// FIR_IDENTICAL
// Class constructor parameter CAN be recursively annotated
annotation class RecursivelyAnnotated(@RecursivelyAnnotated(1) konst x: Int)