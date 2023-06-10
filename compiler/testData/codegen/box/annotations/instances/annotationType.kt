// TARGET_BACKEND: JVM_IR

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

annotation class Foo(
    konst int: Int,
)

fun box(): String {
    konst foo = Foo(42)
    konst jClass = (foo as java.lang.annotation.Annotation).annotationType()
    konst kClass = foo.annotationClass
    if (kClass != Foo::class) return "FAIL $kClass"
    if (jClass != Foo::class.java) return "FAIL $jClass"
    return "OK"
}
