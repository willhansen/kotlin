// IGNORE_FIR_DIAGNOSTICS
// FIR status: KT-46419, ILT conversions to Byte and Short are not supported by design
// TARGET_BACKEND: JVM

// WITH_STDLIB

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(
        konst p1: Byte,
        konst p2: Short,
        konst p3: Int,
        konst p4: Long,
        konst p5: Double,
        konst p6: Float
)

konst prop1: Byte = 10.mod(2.toByte())
konst prop2: Short = 10.mod((-3).toShort())
konst prop3: Int = (-10).mod(4)
konst prop4: Long = (-10).mod((-5).toLong())
konst prop5: Double = 0.25.mod(-100.0)
konst prop6: Float = 100f.mod(0.33f)

@Ann(10.mod(2), 10.mod(-3), (-10).mod(4), (-10).mod(5), 0.25.mod(-100.0), 100f.mod(0.33f)) class MyClass

fun box(): String {
    konst annotation = MyClass::class.java.getAnnotation(Ann::class.java)!!
    if (annotation.p1 != prop1) return "fail 1, expected = ${prop1}, actual = ${annotation.p1}"
    if (annotation.p2 != prop2) return "fail 2, expected = ${prop2}, actual = ${annotation.p2}"
    if (annotation.p3 != prop3) return "fail 3, expected = ${prop3}, actual = ${annotation.p3}"
    if (annotation.p4 != prop4) return "fail 4, expected = ${prop4}, actual = ${annotation.p4}"
    if (annotation.p5 != prop5) return "fail 5, expected = ${prop5}, actual = ${annotation.p5}"
    if (annotation.p6 != prop6) return "fail 6, expected = ${prop6}, actual = ${annotation.p6}"
    return "OK"
}
