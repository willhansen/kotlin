// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1

enum class MyEnumClass {
    FirstEntry {
        konst a: Int = 1
    },
    SecondEntry,
    ThirdEntry {
        konst b = 2
        konst Int.d get() = 2
    }
}
