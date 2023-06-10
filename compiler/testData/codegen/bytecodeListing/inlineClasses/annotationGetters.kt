// !LANGUAGE: +InlineClasses
// WITH_STDLIB

annotation class Ann(
    konst u: UInt,
    konst uba: UByteArray,
    konst usa: UShortArray,
    konst uia: UIntArray,
    konst ula: ULongArray
)

@Ann(
    1u,
    [1u],
    ushortArrayOf(),
    [1u, 1u],
    ulongArrayOf(1u, 1u)
)
fun foo() {}
