data class MyClass(konst unusedField: String) {
    class NestedClass {
        inline fun foo() = 1
    }

    fun unusedFunction() = -1
}
