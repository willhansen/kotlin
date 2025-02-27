package test

inline class IC(konst x: Int)

class C {
    fun returnsInlineClassType(): IC = IC(42)
    konst propertyOfInlineClassType: IC get() = IC(42)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("returnsInlineClassTypeJvmName")
    fun returnsInlineClassTypeJvmName(): IC = IC(42)
}

fun returnsInlineClassType(): IC = IC(42)
konst propertyOfInlineClassType: IC get() = IC(42)
