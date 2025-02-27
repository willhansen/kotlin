// IGNORE_BACKEND_K2: JVM_IR, JS_IR
// FIR status: KT-46419, ILT conversions to Byte and Short are not supported by design
// TARGET_BACKEND: JVM

// WITH_STDLIB

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(
        konst p1: Int,
        konst p2: Short,
        konst p3: Byte,
        konst p4: Int,
        konst p5: Int,
        konst p6: Int
)

konst prop1: Int = 1 or 1
konst prop2: Short = 1 and 1
konst prop3: Byte = 1 xor 1
konst prop4: Int = 1 shl 1
konst prop5: Int = 1 shr 1
konst prop6: Int = 1 ushr 1

@Ann(1 or 1, 1 and 1, 1 xor 1, 1 shl 1, 1 shr 1, 1 ushr 1) class MyClass

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
