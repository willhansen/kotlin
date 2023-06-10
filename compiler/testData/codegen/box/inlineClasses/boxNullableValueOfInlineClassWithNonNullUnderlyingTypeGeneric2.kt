// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

class BoxT<T: Any>(konst boxed: T?)
class BoxAny(konst boxed: Any?)
class BoxFoo(konst boxed: IFoo?)

interface IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Str<T: String>(konst konstue: T) : IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Str2<T: Str<String>>(konst konstue: T): IFoo

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class StrArr(konst konstue: Array<String>): IFoo

fun boxToTypeParameter(x: Str<String>?) = BoxT(x)
fun boxToNullableAny(x: Str<String>?) = BoxAny(x)
fun boxToNullableInterface(x: Str<String>?) = BoxFoo(x)

fun box2ToTypeParameter(x: Str2<Str<String>>?) = BoxT(x)
fun box2ToNullableAny(x: Str2<Str<String>>?) = BoxAny(x)
fun box2ToNullableInterface(x: Str2<Str<String>>?) = BoxFoo(x)

fun boxArrToTypeParameter(x: StrArr?) = BoxT(x)
fun boxArrToNullableAny(x: StrArr?) = BoxAny(x)
fun boxArrToNullableInterface(x: StrArr?) = BoxFoo(x)

fun box(): String {
    if (boxToNullableAny(null).boxed != null) throw AssertionError()
    if (boxToTypeParameter(null).boxed != null) throw AssertionError()
    if (boxToNullableInterface(null).boxed != null) throw AssertionError()

    if (box2ToNullableAny(null).boxed != null) throw AssertionError()
    if (box2ToTypeParameter(null).boxed != null) throw AssertionError()
    if (box2ToNullableInterface(null).boxed != null) throw AssertionError()

    if (boxArrToNullableAny(null).boxed != null) throw AssertionError()
    if (boxArrToTypeParameter(null).boxed != null) throw AssertionError()
    if (boxArrToNullableInterface(null).boxed != null) throw AssertionError()

    return "OK"
}