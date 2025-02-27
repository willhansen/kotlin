// IGNORE_FIR_DIAGNOSTICS
// FIR status: KT-46419, ILT conversions to Byte and Short are not supported by design
// TARGET_BACKEND: JVM

// WITH_STDLIB

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(
        konst b: Byte,
        konst s: Short,
        konst i: Int,
        konst l: Long
)

@Ann(10.floorDiv(3), 10.floorDiv(-4), (-10).floorDiv(5), (-10).floorDiv(-6)) class MyClass

fun box(): String {
    konst annotation = MyClass::class.java.getAnnotation(Ann::class.java)!!
    if (annotation.b != 3.toByte()) return "fail 1"
    if (annotation.s != (-3).toShort()) return "fail 2"
    if (annotation.i != -2) return "fail 3"
    if (annotation.l != 1L) return "fail 4"
    return "OK"
}
