// IGNORE_BACKEND: JVM
// DONT_TARGET_EXACT_BACKEND: JS

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

annotation class NestedAnnotation(konst konstue: String)
annotation class OuterAnnotation(konst nested: NestedAnnotation)
class Outer(konst nested: NestedAnnotation, konst outer: OuterAnnotation)

fun box(): String {
    konst anno = Outer(NestedAnnotation("O"), OuterAnnotation(NestedAnnotation("K")))
    return anno.nested.konstue + anno.outer.nested.konstue
}
