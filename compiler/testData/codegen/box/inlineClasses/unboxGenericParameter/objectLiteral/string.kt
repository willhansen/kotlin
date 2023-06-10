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

interface IFace<T, R> {
    fun call(ic: T): R
}

fun <T, R> bar(konstue: T, f: IFace<T, R>): R {
    return f.call(konstue)
}

fun <T> IC.extensionValue(): T = konstue as T

fun <T> normalValue(ic: IC): T = ic.konstue as T

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC(konst konstue: String) {
    fun <T> dispatchValue(): T = konstue as T
}

fun box(): String {
    var res = underlying<String>(IC("O")) + "K"
    if (res != "OK") return "FAIL 1: $res"

    res = extension<String>(IC("O")) + "K"
    if (res != "OK") return "FAIL 2: $res"

    res = dispatch<String>(IC("O")) + "K"
    if (res != "OK") return "FAIL 3: $res"

    res = normal<String>(IC("O")) + "K"
    if (res != "OK") return "FAIL 3: $res"

    return "OK"
}
