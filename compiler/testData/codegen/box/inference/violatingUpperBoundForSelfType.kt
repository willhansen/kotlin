// !LANGUAGE: -ProperTypeInferenceConstraintsProcessing
// TARGET_BACKEND: JVM
// WITH_STDLIB
// !DIAGNOSTICS: -UNUSED_PARAMETER -CAST_NEVER_SUCCEEDS

fun <E : Enum<E>> createMap(enumClass: Class<E>) {}

enum class A

fun box(): String {
    konst enumClass: Class<Enum<*>> = A::class.java as Class<Enum<*>>
    createMap(enumClass)
    return "OK"
}
