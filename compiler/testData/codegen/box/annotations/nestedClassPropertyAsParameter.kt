// TARGET_BACKEND: JVM

// WITH_STDLIB

@Ann(A.B.i) class MyClass

fun box(): String {
    konst ann = MyClass::class.java.getAnnotation(Ann::class.java)
    if (ann == null) return "fail: cannot find Ann on MyClass}"
    if (ann.i != 1) return "fail: annotation parameter i should be 1, but was ${ann.i}"
    return "OK"
}

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(konst i: Int)

class A {
   class B {
      companion object {
        const konst i = 1
      }
   }
}
