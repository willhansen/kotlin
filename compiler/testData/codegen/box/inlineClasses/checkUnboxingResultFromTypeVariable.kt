// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Result<T>(konst a: Any?) {
    fun typed(): T = a as T
}

fun <T> takeResult(r: Result<T>) {}
fun takeResultOfInt(r: Result<Int>) {}
fun takeInt(i: Int) {}


fun box(): String {
    konst asInt = Result<Int>(19)
    konst asString = Result<String>("sample")
    konst asResult = Result<Result<Int>>(asInt)
    konst asResultCtor = Result<Result<Int>>(Result<Int>(10))

    takeResult(asInt)
    takeResult(asString)
    takeResult(asResult)
    takeResult(asResultCtor)

    takeResultOfInt(asInt)
    takeInt(asInt.typed())

    konst unboxedInt = asInt.typed()
    konst unboxedString = asString.typed()
    konst unboxedResult = asResult.typed()
    konst unboxedAsCtor = asResultCtor.typed()

    if (unboxedInt != 19) return "fail 1"
    if (unboxedString != "sample") return "fail 2"
    if (unboxedResult.typed() != 19) return "fail 3"
    if (unboxedAsCtor.typed() != 10) return "fail 4"

    if (asResult.typed().typed() != 19) return "fail 5"
    if (asResultCtor.typed().typed() != 10) return "fail 6"

    return "OK"
}
