// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Bar(konst y: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T>(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo2<T>(konst x: Foo<T>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo3<T>(konst x: Bar)

fun testValueParameter(z: Foo<Any>) = z.x
fun testValueParameter2(z: Foo2<Any>) = z.x.x
fun testValueParameter3(z: Foo3<Any>) = z.x.y

fun testReturnType(x: Int) = Foo<Any>(x)
fun testReturnType2(x: Int) = Foo2<Any>(Foo<Any>(x))
fun testReturnType3(x: Int) = Foo3<Any>(Bar(x))

fun testGenericTypeArgumentInValueParameter(zs: List<Foo<Any>>) = zs[0].x
fun testGenericTypeArgumentInValueParameter2(zs: List<Foo2<Any>>) = zs[0].x.x
fun testGenericTypeArgumentInValueParameter3(zs: List<Foo3<Any>>) = zs[0].x.y

fun testGenericTypeArgumentInReturnType(x: Int) = listOf(Foo<Any>(x))
fun testGenericTypeArgumentInReturnType2(x: Int) = listOf(Foo2(Foo<Any>(x)))
fun testGenericTypeArgumentInReturnType3(x: Int) = listOf(Foo3<Any>(Bar(x)))

fun box(): String {
    if (testValueParameter(Foo(42)) != 42) throw AssertionError()
    if (testValueParameter2(Foo2(Foo<Any>(42))) != 42) throw AssertionError()
    if (testValueParameter3(Foo3(Bar(42))) != 42) throw AssertionError()

    if (testReturnType(42).x != 42) throw AssertionError()
    if (testReturnType2(42).x.x != 42) throw AssertionError()
    if (testReturnType3(42).x.y != 42) throw AssertionError()

    if (testGenericTypeArgumentInValueParameter(listOf(Foo<Any>(42))) != 42) throw AssertionError()
    if (testGenericTypeArgumentInValueParameter2(listOf(Foo2(Foo<Any>(42)))) != 42) throw AssertionError()
    if (testGenericTypeArgumentInValueParameter3(listOf(Foo3<Any>(Bar(42)))) != 42) throw AssertionError()

    if (testGenericTypeArgumentInReturnType(42)[0].x != 42) throw AssertionError()
    if (testGenericTypeArgumentInReturnType2(42)[0].x.x != 42) throw AssertionError()
    if (testGenericTypeArgumentInReturnType3(42)[0].x.y != 42) throw AssertionError()

    return "OK"
}