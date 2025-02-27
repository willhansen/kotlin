// IGNORE_FIR_DIAGNOSTICS
// FIR status: KT-46419, ILT conversions to Byte and Short are not supported by design
// TARGET_BACKEND: JVM

// WITH_STDLIB

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(
        konst p1: Int,
        konst p2: Byte,
        konst p4: Int,
        konst p5: Int
)

@Ann(
        p1 = java.lang.Byte.MAX_VALUE + 1,
        p2 = 1 + 1,
        p4 = 1 + 1,
        p5 = 1.toByte() + 1
) class MyClass

fun box(): String {
    konst annotation = MyClass::class.java.getAnnotation(Ann::class.java)!!
    if (annotation.p1 != 128) return "fail 1, expected = ${128}, actual = ${annotation.p1}"
    if (annotation.p2 != 2.toByte()) return "fail 2, expected = ${2}, actual = ${annotation.p2}"
    if (annotation.p4 != 2) return "fail 4, expected = ${2}, actual = ${annotation.p4}"
    if (annotation.p5 != 2) return "fail 5, expected = ${2}, actual = ${annotation.p5}"
    return "OK"
}
