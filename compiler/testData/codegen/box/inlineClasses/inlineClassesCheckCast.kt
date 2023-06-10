// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class AsAny<T>(konst x: Any?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class AsInt(konst x: Int)

inline fun <reified T> Any?.checkcast(): T = this as T

object Reference {
    fun <T, R> transform(a: AsAny<T>): AsAny<R> = a as AsAny<R>
    fun <T, R> transformNullable(a: AsAny<T>?): AsAny<R> = a as AsAny<R>
    fun <T, R> transformToNullable(a: AsAny<T>): AsAny<R>? = a as AsAny<R>
    fun <T, R> transformToNullableTarget(a: AsAny<T>): AsAny<R>? = a as AsAny<R>?
    fun <T, R> transformNullableToNullableTarget(a: AsAny<T>?): AsAny<R>? = a as AsAny<R>?
}

object Primitive {
    fun transform(a: AsInt): AsInt = a as AsInt
    fun transformNullable(a: AsInt?): AsInt = a as AsInt
    fun transformToNullable(a: AsInt): AsInt? = a as AsInt
    fun transformToNullableTarget(a: AsInt): AsInt? = a as AsInt?
    fun transformNullableToNullableTarget(a: AsInt?): AsInt? = a as AsInt?
}

fun box(): String {
    konst a = AsAny<Int>(42)
    konst b1 = Reference.transform<Int, Number>(a)
    konst b2 = Reference.transformNullable<Int, Number>(a)
    konst b3 = Reference.transformToNullable<Int, Number>(a)
    konst b4 = Reference.transformToNullableTarget<Int, Number>(a)
    konst b5 = Reference.transformNullableToNullableTarget<Int, Number>(a)
    konst b6 = Reference.transformNullableToNullableTarget<Int, Number>(null)

    konst b7 = a.checkcast<AsAny<Number>>()
    if (b7.x != a.x) return "Fail 1"

    konst c = AsInt(42)
    konst d1 = Primitive.transform(c)
    konst d2 = Primitive.transformNullable(c)
    konst d3 = Primitive.transformToNullable(c)
    konst d4 = Primitive.transformToNullableTarget(c)
    konst d5 = Primitive.transformNullableToNullableTarget(c)
    konst d6 = Primitive.transformNullableToNullableTarget(null)

    konst d7 = c.checkcast<AsInt>()
    if (d7.x != c.x) return "Fail 2"

    return "OK"
}