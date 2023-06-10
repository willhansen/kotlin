// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Location @JvmOverloads constructor(konst konstue: String? = "OK")

fun box(): String = Location().konstue!!
