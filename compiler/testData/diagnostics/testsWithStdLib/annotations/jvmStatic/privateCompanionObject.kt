// FIR_IDENTICAL
// Issue: KT-25114
// !DIAGNOSTICS: -UNUSED_PARAMETER

class WithPrivateCompanion {
    private companion object {
        @JvmStatic
        konst staticVal1: Int = 42

        konst staticVal2: Int
            @JvmStatic get() = 42

        @get:JvmStatic
        konst staticVal3: Int = 42

        @JvmStatic
        var staticVar1: Int = 42

        var staticVar2: Int
            @JvmStatic get() = 42
            @JvmStatic set(konstue) {}

        @get: JvmStatic
        @set: JvmStatic
        var staticVar3: Int = 42

        @JvmStatic
        fun staticFunction() {}
    }
}
