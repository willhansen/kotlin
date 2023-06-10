// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

class BoxT<T>(konst boxed: T)
class BoxAny(konst boxed: Any?)
class BoxFoo(konst boxed: IFoo?)

interface IFoo

interface Marker

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcInt(konst i: Int): Marker

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class I32<T: Marker>(konst konstue: T?) : IFoo where T: IcInt

fun <T: Marker> boxToTypeParameter(x: I32<T>?) where T: IcInt = BoxT(x)
fun <T: Marker> boxToNullableAny(x: I32<T>?) where T: IcInt = BoxAny(x)
fun <T: Marker> boxToNullableInterface(x: I32<T>?) where T: IcInt = BoxFoo(x)

fun <T: Marker> useNullableI32(x: I32<T>?) where T: IcInt {
    if (x != null) throw AssertionError()
}

fun box(): String {
    useNullableI32(boxToTypeParameter<IcInt>(null).boxed)
    useNullableI32(boxToNullableAny<IcInt>(null).boxed as I32<IcInt>?)
    useNullableI32(boxToNullableInterface<IcInt>(null).boxed as I32<IcInt>?)

    return "OK"
}