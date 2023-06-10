// TARGET_BACKEND: JVM_IR
// TARGET_BACKEND: JS_IR
// TARGET_BACKEND: NATIVE
// WITH_STDLIB

// MODULE: lib
// FILE: lib.kt

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class TypeAnnotation(konst str: String)

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class Nested(konst a: TypeAnnotation)

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class NestedArray(konst a: Array<TypeAnnotation>)

konst a: @Nested(TypeAnnotation("Int" <!EVALUATED("IntAnno")!>+ "Anno"<!>)) Int = 1
konst b: @NestedArray([TypeAnnotation("Element1" <!EVALUATED("Element1Anno")!>+ "Anno"<!>), TypeAnnotation("Element2" <!EVALUATED("Element2Anno")!>+ "Anno"<!>)]) Int = 1

// MODULE: main
// FILE: main.kt

fun box(): String {
    return "OK"
}
