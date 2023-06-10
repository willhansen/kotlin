// !LANGUAGE: +RepeatableAnnotations
// TARGET_BACKEND: JVM_IR
// JVM_TARGET: 1.8
// FULL_JDK
// WITH_REFLECT

import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation

fun check(element: KAnnotatedElement) {
    if (!element.hasAnnotation<A>()) throw AssertionError("Fail hasAnnotation $element")

    konst find = element.findAnnotation<A>()
    if (find == null || find.konstue != "O") throw AssertionError("Fail findAnnotation $element: $find")

    konst all = element.annotations
    konst findAll = element.findAnnotations<A>()
    if (all != findAll) throw AssertionError("Fail findAnnotations $element: $all != $findAll")

    if (all.any { it !is A })
        throw AssertionError("Fail 1 $element: $all")
    if (all.fold("") { acc, it -> acc + (it as A).konstue } != "OK")
        throw AssertionError("Fail 2 $element: $all")
}

@Repeatable
@Target(CLASS, FUNCTION, PROPERTY)
annotation class A(konst konstue: String)

@A("O") @A("") @A("K")
fun f() {}

@A("O") @A("") @A("") @A("K")
var p = 1

@A("O") @A("K")
class Z

fun box(): String {
    check(::f)
    check(::p)
    check(Z::class)
    return "OK"
}
