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

@Ann(1 / 1, 1 / 1, 1 / 1, 1 / 1) class MyClass

fun box(): String {
    konst annotation = MyClass::class.java.getAnnotation(Ann::class.java)!!
    if (annotation.b != 1.toByte()) return "fail 1"
    if (annotation.s != 1.toShort()) return "fail 2"
    if (annotation.i != 1) return "fail 3"
    if (annotation.l != 1.toLong()) return "fail 4"
    return "OK"
}

// EXPECTED: Ann[b = IntegerValueType(1): IntegerValueType(1), i = IntegerValueType(1): IntegerValueType(1), l = IntegerValueType(1): IntegerValueType(1), s = IntegerValueType(1): IntegerValueType(1)]
