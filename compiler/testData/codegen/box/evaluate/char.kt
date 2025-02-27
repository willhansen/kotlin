// TARGET_BACKEND: JVM

// WITH_STDLIB

package test

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(konst c1: Int)

@Ann('a' - 'a') class MyClass

fun box(): String {
    konst annotation = MyClass::class.java.getAnnotation(Ann::class.java)!!
    if (annotation.c1 != 0) return "fail : expected = ${1}, actual = ${annotation.c1}"
    return "OK"
}
