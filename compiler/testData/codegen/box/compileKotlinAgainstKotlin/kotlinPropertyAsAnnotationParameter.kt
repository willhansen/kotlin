// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.kt

package a

const konst i = 2
const konst s = 2.toShort()
const konst f = 2.0.toFloat()
const konst d = 2.0
const konst l = 2L
const konst b = 2.toByte()
const konst bool = true
const konst c = 'c'
const konst str = "str"

const konst i2 = i
const konst s2 = s
const konst f2 = f
const konst d2 = d
const konst l2 = l
const konst b2 = b
const konst bool2 = bool
const konst c2 = c
const konst str2 = str

// MODULE: main(lib)
// FILE: B.kt

import a.*

@Ann(i, s, f, d, l, b, bool, c, str)
class MyClass1

@Ann(i2, s2, f2, d2, l2, b2, bool2, c2, str2)
class MyClass2

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(
        konst i: Int,
        konst s: Short,
        konst f: Float,
        konst d: Double,
        konst l: Long,
        konst b: Byte,
        konst bool: Boolean,
        konst c: Char,
        konst str: String
)

fun box(): String {
    // Trigger annotation loading
    (MyClass1() as java.lang.Object).getClass().getAnnotations()
    (MyClass2() as java.lang.Object).getClass().getAnnotations()
    return "OK"
}
