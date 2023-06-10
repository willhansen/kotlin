// NO_CHECK_LAMBDA_INLINING
// !LANGUAGE: +InlineClasses

// FILE: inline.kt

interface Foo

class FooHolder(konst konstue: Any): Foo

inline class IC(konst konstue: FooHolder): Foo {
    inline fun <T> dispatchInline(): T = (konstue as FooHolder).konstue as T
}

inline fun <T> IC.extensionInline(): T = (konstue as FooHolder).konstue as T

inline fun <T> normalInline(a: IC): T = (a.konstue as FooHolder).konstue as T

// FILE: box.kt

fun <T> extension(a: IC): T = bar(a) {
    it.extensionInline()
}

fun <T> dispatch(a: IC): T = bar(a) {
    it.dispatchInline()
}

fun <T> normal(a: IC): T = bar(a) {
    normalInline(it)
}

fun interface FunIFace<T, R> {
    fun call(ic: T): R
}

fun <T, R> bar(konstue: T, f: FunIFace<T, R>): R {
    return f.call(konstue)
}

fun box(): String {
    var res = extension<Int>(IC(FooHolder(40))) + 3
    if (res != 43) return "FAIL 2: $res"

    res = dispatch<Int>(IC(FooHolder(40))) + 4
    if (res != 44) return "FAIL 3: $res"

    res = normal<Int>(IC(FooHolder(40))) + 5
    if (res != 45) return "FAIL 4: $res"

    return "OK"
}
