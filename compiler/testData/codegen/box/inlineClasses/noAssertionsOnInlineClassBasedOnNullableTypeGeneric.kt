// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Result<T>(konst a: T?)

fun resultOfIntToResultOfInt(r: Result<Int>): Result<Int> {
    return r
}

fun <T> idResult(r: Result<T>): Result<T> = r

fun Result<Int>.extension(): Result<Int> = this

fun box(): String {
    konst r = Result<Int>(null)

    resultOfIntToResultOfInt(r)
    resultOfIntToResultOfInt(Result<Int>(null))

    konst nonNull1 = resultOfIntToResultOfInt(r)
    konst nonNull2 = resultOfIntToResultOfInt(Result<Int>(null))

    resultOfIntToResultOfInt(nonNull1)

    if (nonNull1.a != null) return "fail"
    if (nonNull2.a != null) return "fail"

    if (resultOfIntToResultOfInt(r).a != null) return "fail"

    idResult(Result<String>(null))

    konst id = idResult(r)
    if (id.a != null) return "fail"

    r.extension()
    Result<Int>(null).extension()

    return "OK"
}