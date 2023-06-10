// FIR_IDENTICAL

class GenericClass<T>(konst konstue: T) {
    fun withNewValue(newValue: T) = GenericClass(newValue)
}
