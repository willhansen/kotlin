// FIR_IDENTICAL
// ISSUE: KT-57247

typealias Aliased = String
annotation class Tag(vararg konst tags: Aliased) // K1: ok, K2: INVALID_TYPE_OF_ANNOTATION_MEMBER
