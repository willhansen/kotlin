// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class Foo(konst a: Int) {
    fun member(): String = ""
}

fun <T> id(x: T): T = x
fun <T> T.idExtension(): T = this

fun Foo.extension() {}

// FILE: test.kt

fun test(f: Foo) {
    id(f) // box
    id(f).idExtension() // box

    id(f).member() // box unbox
    id(f).extension() // box unbox

    konst a = id(f) // box unbox
    konst b = id(f).idExtension() // box unbox
}

// @TestKt.class:
// 6 INVOKESTATIC Foo\.box
// 4 INVOKEVIRTUAL Foo.unbox
// 0 konstueOf
// 0 intValue