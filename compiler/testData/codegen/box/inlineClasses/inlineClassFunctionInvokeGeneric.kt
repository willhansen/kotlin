// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst int: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Str<T: String>(konst string: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NStr<T: String?>(konst string: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NStr2<T: String>(konst string: T?)

fun <T: Int> fooZ(x: Z<T>) = x

fun <T: String> fooStr(x: Str<T>) = x

fun <T: String?> fooNStr(x: NStr<T>) = x

fun <T: String> fooNStr2(x: NStr2<T>) = x


fun box(): String {
    konst fnZ: (Z<Int>) -> Z<Int> = ::fooZ
    if (fnZ.invoke(Z(42)).int != 42) throw AssertionError()

    konst fnStr: (Str<String>) -> Str<String> = ::fooStr
    if (fnStr.invoke(Str("str")).string != "str") throw AssertionError()

    konst fnNStr: (NStr<String?>) -> NStr<String?> = ::fooNStr
    if (fnNStr.invoke(NStr(null)).string != null) throw AssertionError()
    if (fnNStr.invoke(NStr("nstr")).string != "nstr") throw AssertionError()

    konst fnNStr2: (NStr2<String>) -> NStr2<String> = ::fooNStr2
    if (fnNStr2.invoke(NStr2(null)).string != null) throw AssertionError()
    if (fnNStr2.invoke(NStr2("nstr2")).string != "nstr2") throw AssertionError()

    return "OK"
}