// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst int: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Str(konst string: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NStr(konst string: String?)

fun fooZ(x: Z) = x

fun fooStr(x: Str) = x

fun fooNStr(x: NStr) = x


fun box(): String {
    konst fnZ = ::fooZ
    if (fnZ.invoke(Z(42)).int != 42) throw AssertionError()

    konst fnStr = ::fooStr
    if (fnStr.invoke(Str("str")).string != "str") throw AssertionError()

    konst fnNStr = ::fooNStr
    if (fnNStr.invoke(NStr(null)).string != null) throw AssertionError()
    if (fnNStr.invoke(NStr("nstr")).string != "nstr") throw AssertionError()

    return "OK"
}