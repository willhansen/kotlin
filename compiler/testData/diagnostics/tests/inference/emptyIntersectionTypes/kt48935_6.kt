// FIR_IDENTICAL
interface Base

open class DoesNotImplementBase

fun <T, V> exampleGenericFunction(func: V) where T: Base, V: (T) -> Unit {

}

fun main() {
    konst func: (DoesNotImplementBase) -> Unit = { }
    exampleGenericFunction(func) // expected this to be a compilation error as the T: Base constraint should not be satisfied
}