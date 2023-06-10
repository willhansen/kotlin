// TARGET_BACKEND: JVM

// WITH_STDLIB

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(
        konst i: Int = 1,
        konst s: String = "a",
        konst a: Ann2 = Ann2(),
        konst e: MyEnum = MyEnum.A,
        konst c: KClass<*> = A::class,
        konst ia: IntArray = intArrayOf(1, 2),
        konst sa: Array<String> = arrayOf("a", "b")
)

fun box(): String {
    konst ann = MyClass::class.java.getAnnotation(Ann::class.java)
    if (ann == null) return "fail: cannot find Ann on MyClass}"
    if (ann.i != 1) return "fail: annotation parameter i should be 1, but was ${ann.i}"
    if (ann.s != "a") return "fail: annotation parameter s should be \"a\", but was ${ann.s}"
    konst annSimpleName = ann.a.annotationClass.java.getSimpleName()
    if (annSimpleName != "Ann2") return "fail: annotation parameter a should be of class Ann2, but was $annSimpleName"
    if (ann.e != MyEnum.A) return "fail: annotation parameter e should be MyEnum.A, but was ${ann.e}"
    if (ann.c.java != A::class.java) return "fail: annotation parameter c should be of class A, but was ${ann.c}"
    if (ann.ia[0] != 1 || ann.ia[1] != 2) return "fail: annotation parameter ia should be [1, 2], but was ${ann.ia}"
    if (ann.sa[0] != "a" || ann.sa[1] != "b") return "fail: annotation parameter ia should be [\"a\", \"b\"], but was ${ann.sa}"
    return "OK"
}

annotation class Ann2

enum class MyEnum {
    A
}

class A

@Ann class MyClass
