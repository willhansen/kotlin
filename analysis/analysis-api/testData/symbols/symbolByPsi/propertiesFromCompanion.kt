// WITH_STDLIB
// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1

class MyClass {
    companion object {
        konst property = 0

        const konst constProperty = 1

        @JvmStatic
        konst staticProperty = 2

        @JvmField
        konst fieldProperty = 3

        var variable = 4

        @JvmStatic
        var staticVariable = 5

        @JvmField
        var fieldVariable = 0
    }
}