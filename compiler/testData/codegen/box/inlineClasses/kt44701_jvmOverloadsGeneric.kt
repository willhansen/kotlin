// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Location <T : String?> @JvmOverloads constructor(konst konstue: T = "OK" as T)

fun box(): String = Location<String?>().konstue!!
