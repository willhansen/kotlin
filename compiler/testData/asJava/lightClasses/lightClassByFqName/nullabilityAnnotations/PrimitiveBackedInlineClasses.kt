// PrimitiveBackedInlineClassesKt
// WITH_STDLIB

@JvmName("getUInt") fun geUInt(): UInt = 42U
@JvmName("getNullableUInt") fun getNullableUInt(): UInt? = null
@JvmName("getInlineClass") fun getInlineClass(): InlineClass = InlineClass(42)
@JvmName("getNullableInlineClass") fun getNullableUInlineClass(): InlineClass? = null


@JvmInline konstue class InlineClass(konst data: Int)


// FIR_COMPARISON