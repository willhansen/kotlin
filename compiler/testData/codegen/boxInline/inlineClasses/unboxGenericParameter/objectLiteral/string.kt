// NO_CHECK_LAMBDA_INLINING
// !LANGUAGE: +InlineClasses
// FILE: inline.kt

inline class IC(konst konstue: String) {
    inline fun <T> dispatchInline(): T = konstue as T
}

inline fun <T> IC.extensionInline(): T = konstue as T

inline fun <T> normalInline(a: IC): T = a.konstue as T

// FILE: box.kt

fun <T> extension(a: IC): T = bar(a, object : IFace<IC, T> {
    override fun call(it: IC): T = it.extensionInline()
})

fun <T> dispatch(a: IC): T = bar(a, object : IFace<IC, T> {
    override fun call(it: IC): T = it.dispatchInline()
})

fun <T> normal(a: IC): T = bar(a, object : IFace<IC, T> {
    override fun call(it: IC): T = normalInline(it)
})

interface IFace<T, R> {
    fun call(ic: T): R
}

fun <T, R> bar(konstue: T, f: IFace<T, R>): R {
    return f.call(konstue)
}

fun box(): String {
    var res = extension<String>(IC("O")) + "K"
    if (res != "OK") return "FAIL 2: $res"

    res = dispatch<String>(IC("O")) + "K"
    if (res != "OK") return "FAIL 3: $res"

    res = normal<String>(IC("O")) + "K"
    if (res != "OK") return "FAIL 3: $res"

    return "OK"
}
