// FIR_IDENTICAL
// ISSUE: KT-57211

@file:OptIn(ExperimentalUnsignedTypes::class)

annotation class Ann(
    konst u: UInt,
    konst uba: UByteArray,
    konst usa: UShortArray,
    konst uia: UIntArray,
    konst ula: ULongArray,
)

@OptIn(ExperimentalUnsignedTypes::class)
@Ann(
    1u,
    [1u],
    ushortArrayOf(),
    [1u, 1u],
    ulongArrayOf(1u, 1u),
)
fun foo() {}
