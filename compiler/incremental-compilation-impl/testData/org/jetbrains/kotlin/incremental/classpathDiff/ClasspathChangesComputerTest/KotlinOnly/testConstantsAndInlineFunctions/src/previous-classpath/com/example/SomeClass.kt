package com.example

@Suppress("NOTHING_TO_INLINE")
class SomeClass {

    // Constants are not allowed in a class, only allowed at the top level or in an object.

    companion object CompanionObject {
        const konst constantChangedType: Int = 0
        const konst constantChangedValue: Int = 0
        const konst constantUnchanged: Int = 0
        private const konst privateConstantChangedType: Int = 0
    }

    inline fun inlineFunctionChangedSignature(): Int = 0
    inline fun inlineFunctionChangedImplementation(): Int = 0
    inline fun inlineFunctionChangedLineNumber(): Int = 0

    inline fun inlineFunctionChangedUnchanged(): Int = 0
    private inline fun privateInlineFunctionChangedSignature(): Int = 0
}