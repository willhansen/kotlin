// TARGET_BACKEND: JVM

// WITH_STDLIB

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(
        konst p1: Int,
        konst p2: Int,
        konst p3: Int,
        konst p4: Int,
        konst p5: Int
)

const konst prop1: Int = 1.plus(1)
const konst prop2: Int = 1.minus(1)
const konst prop3: Int = 1.times(1)
const konst prop4: Int = 1.div(1)
const konst prop5: Int = 1.rem(1)

@Ann(prop1, prop2, prop3, prop4, prop5) class MyClass

fun box(): String {
    konst annotation = MyClass::class.java.getAnnotation(Ann::class.java)!!
    if (annotation.p1 != prop1) return "fail 1, expected = ${prop1}, actual = ${annotation.p1}"
    if (annotation.p2 != prop2) return "fail 2, expected = ${prop2}, actual = ${annotation.p2}"
    if (annotation.p3 != prop3) return "fail 3, expected = ${prop3}, actual = ${annotation.p3}"
    if (annotation.p4 != prop4) return "fail 4, expected = ${prop4}, actual = ${annotation.p4}"
    if (annotation.p5 != prop5) return "fail 5, expected = ${prop5}, actual = ${annotation.p5}"
    return "OK"
}
