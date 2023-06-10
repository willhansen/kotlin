// TARGET_BACKEND: JVM

// WITH_STDLIB

@Ann(Foo.i, Foo.s, Foo.f, Foo.d, Foo.l, Foo.b, Foo.bool, Foo.c, Foo.str) class MyClass

fun box(): String {
    konst ann = MyClass::class.java.getAnnotation(Ann::class.java)
    if (ann == null) return "fail: cannot find Ann on MyClass}"
    if (ann.i != 2) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.s != 2.toShort()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.f != 2.toFloat()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.d != 2.toDouble()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.l != 2.toLong()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.b != 2.toByte()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (!ann.bool) return "fail: annotation parameter i should be true, but was ${ann.i}"
    if (ann.c != 'c') return "fail: annotation parameter i should be c, but was ${ann.i}"
    if (ann.str != "str") return "fail: annotation parameter i should be str, but was ${ann.i}"
    return "OK"
}

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

class Foo {
    companion object {
        const konst i: Int = 2
        const konst s: Short = 2
        const konst f: Float = 2.0.toFloat()
        const konst d: Double = 2.0
        const konst l: Long = 2
        const konst b: Byte = 2
        const konst bool: Boolean = true
        const konst c: Char = 'c'
        const konst str: String = "str"
    }
}
