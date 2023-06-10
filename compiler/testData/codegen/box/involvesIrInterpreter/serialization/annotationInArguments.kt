// TARGET_BACKEND: JVM_IR
// TARGET_BACKEND: JS_IR
// TARGET_BACKEND: NATIVE

// MODULE: lib
// FILE: lib.kt

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Annotation(konst str: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AnnotationWithAnnotation(konst anno: Annotation)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AnnotationWithAnnotationWithAnnotation(konst anno: AnnotationWithAnnotation)

@AnnotationWithAnnotation(Annotation("Str" <!EVALUATED("String")!>+ "ing"<!>))
class A

@AnnotationWithAnnotationWithAnnotation(AnnotationWithAnnotation(Annotation("Str" <!EVALUATED("String")!>+ "ing"<!>)))
class B

// MODULE: main
// FILE: main.kt

fun box(): String {
    return "OK"
}
