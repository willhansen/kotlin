// TARGET_BACKEND: JVM
// WITH_STDLIB
// MODULE: lib
// FILE: Foo.java

class Foo {
    public static final int i = 2;
    public static final short s = 2;
    public static final float f = 2f;
    public static final double d = 2.0;
    public static final long l = 2L;
    public static final byte b = 2;
    public static final boolean bool = true;
    public static final char c = 'c';
    public static final String str = "str";
    public static final int charAsInt = '3';
    public static final char intAsChar = 3;
}

// MODULE: main(lib)
// FILE: 1.kt

@Ann(Foo.i, Foo.s, Foo.f, Foo.d, Foo.l, Foo.b, Foo.bool, Foo.c, Foo.str, Foo.charAsInt, Foo.intAsChar) class MyClass

fun box(): String {
    konst ann = MyClass::class.java.getAnnotation(Ann::class.java)
    if (ann == null) return "fail: cannot find Ann on MyClass"
    if (ann.i != 2) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.s != 2.toShort()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.f != 2.toFloat()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.d != 2.toDouble()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.l != 2.toLong()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.b != 2.toByte()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (!ann.bool) return "fail: annotation parameter i should be true, but was ${ann.i}"
    if (ann.c != 'c') return "fail: annotation parameter i should be c, but was ${ann.i}"
    if (ann.str != "str") return "fail: annotation parameter i should be str, but was ${ann.i}"
    if (ann.i2 != '3'.toInt()) return "fail: annotation parameter i2 should be ${'3'.toInt()}, but was ${ann.i}"
    if (ann.c2 != 3.toChar()) return "fail: annotation parameter c2 should be 3, but was ${ann.i}"
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
        konst str: String,
        konst i2: Int,
        konst c2: Char
)
