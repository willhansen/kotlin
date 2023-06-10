// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

inline class ConstructorWithDefaultVisibility(konst x: Int)
inline class PublicConstructor public constructor(konst x: Int)
inline class InternalConstructor internal constructor(konst x: Int)
inline class ProtectedConstructor protected constructor(konst x: Int)
inline class PrivateConstructor private constructor(konst x: Int)
