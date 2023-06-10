// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

fun <T> underlying(a: IC): T = bar(a, object : IFace<IC, T> {
    override fun call(ic: IC): T = ic.konstue as T
})

fun <T> extension(a: IC): T = bar(a, object : IFace<IC, T> {
    override fun call(ic: IC): T = ic.extensionValue()
})

fun <T> dispatch(a: IC): T = bar(a, object : IFace<IC, T> {
    override fun call(ic: IC): T = ic.dispatchValue()
})

fun <T> normal(a: IC): T = bar(a, object : IFace<IC, T> {
    override fun call(ic: IC): T = normalValue(ic)
})

fun <T> IC.extensionValue(): T = konstue as T

fun <T> normalValue(ic: IC): T = ic.konstue as T

interface IFace<T, R> {
    fun call(ic: T): R
}

fun <T, R> bar(konstue: T, f: IFace<T, R>): R {
    return f.call(konstue)
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC(konst konstue: Any) {
    fun <T> dispatchValue(): T = konstue as T
}

fun box(): String {
    var res = underlying<Int>(IC(40)) + 2
    if (res != 42) return "FAIL 1: $res"

    res = extension<Int>(IC(40)) + 3
    if (res != 43) return "FAIL 2: $res"

    res = dispatch<Int>(IC(40)) + 4
    if (res != 44) return "FAIL 3: $res"

    res = normal<Int>(IC(40)) + 5
    if (res != 45) return "FAIL 4: $res"

    return "OK"
}