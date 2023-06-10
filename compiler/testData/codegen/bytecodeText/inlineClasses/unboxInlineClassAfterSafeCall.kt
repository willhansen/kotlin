// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class Foo(konst x: Int) {
    fun member() {}
}

// FILE: test.kt

fun Foo.extension() {}
fun <T> T.genericExtension() {}

fun test(f: Foo?) {
    f?.member() // unbox
    f?.extension() // unbox
    f?.genericExtension()
}

// @TestKt.class:
// 0 INVOKESTATIC Foo\$Erased.box
// 2 INVOKEVIRTUAL Foo.unbox

// 0 konstueOf
// 0 intValue