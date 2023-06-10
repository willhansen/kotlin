// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

fun <T1> underlying(a: IC): T1 = bar(a) { it.konstue as T1 }

fun <T2> extension(a: IC): T2 = bar(a) { it.extensionValue() }

fun <T3> dispatch(a: IC): T3 = bar(a) { it.dispatchValue() }

fun <T4> normal(a: IC): T4 = bar(a) { normalValue(it) }

fun interface FunIFace<T0, R> {
    fun call(ic: T0): R
}

fun <T5, R> bar(konstue: T5, f: FunIFace<T5, R>): R {
    return f.call(konstue)
}

fun <T6> IC.extensionValue(): T6 = konstue as T6

fun <T7> normalValue(ic: IC): T7 = ic.konstue as T7

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC(konst konstue: Int) {
    fun <T8> dispatchValue(): T8 = konstue as T8
}

fun box(): String {
    var res = underlying<Int>(IC(40)) + 2
    if (res != 42) "FAIL 1: $res"

    res = extension<Int>(IC(40)) + 3
    if (res != 43) "FAIL 2: $res"

    res = dispatch<Int>(IC(40)) + 4
    if (res != 44) "FAIL 3: $res"

    res = normal<Int>(IC(40)) + 5
    if (res != 45) return "FAIL 4: $res"

    return "OK"
}