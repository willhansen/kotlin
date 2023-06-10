// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter
// FILE: 1.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class C<T: String>(konst x: T)

// FILE: 2.kt
@file:JvmMultifileClass
@file:JvmName("Multifile")

private var result: String? = null

var String.k: C<String>
    get() = C(this + result!!)
    set(konstue) { result = konstue.x }

// FILE: 3.kt
fun box(): String {
    "".k = C("K")
    return "O".k.x
}
