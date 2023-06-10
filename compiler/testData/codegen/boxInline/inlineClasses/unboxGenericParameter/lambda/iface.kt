// NO_CHECK_LAMBDA_INLINING
// !LANGUAGE: +InlineClasses

// FILE: inline.kt

interface Foo

class FooHolder(konst konstue: Any): Foo

inline class IC(konst konstue: Foo): Foo {
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

fun <T, R> bar(konstue: T, f: (T) -> R): R {
    return f(konstue)
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
