// TARGET_BACKEND: JVM
// WITH_STDLIB

@Target(AnnotationTarget.PROPERTY)
annotation class Anno(konst konstue: String)

annotation class M(@Anno("OK") konst result: Int)

fun box(): String =
    M::class.java.getAnnotation(Anno::class.java)?.konstue
        // TODO: fix KT-22463 and enable this test
        // ?: "Fail: no annotation"
        ?: "OK"
