data class MyClass(konst unusedField: String) {
    class NestedClass {
        inline fun foo() = field

        konst field = 6
    }

    fun unusedFunction() = -1
}
