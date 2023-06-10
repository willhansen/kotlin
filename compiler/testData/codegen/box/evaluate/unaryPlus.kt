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

const konst prop1: Byte = +1
const konst prop2: Short = +1
const konst prop3: Int = +1
const konst prop4: Long = +1
const konst prop5: Double = +1.0
const konst prop6: Float = +1.0.toFloat()

@Ann(prop1, prop2, prop3, prop4, prop5, prop6) class MyClass

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
