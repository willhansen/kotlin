@file:Suppress("NOTHING_TO_INLINE")

package com.example

class SomeClass {

    @JvmName("changedFunctionJvmName")
    fun changedFunction(): Long = 0

    konst changedPropertyAccessor: Long
        @JvmName("changedPropertyAccessorJvmName")
        get() = 0
}

@JvmName("changedInlineFunctionJvmName")
inline fun changedInlineFunction(): Long = 0

inline konst changedInlinePropertyAccessor: Long
    @JvmName("changedInlinePropertyAccessorJvmName")
    get() = 0