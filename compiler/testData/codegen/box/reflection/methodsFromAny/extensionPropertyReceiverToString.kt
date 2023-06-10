// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.KProperty1
import kotlin.test.assertEquals

fun check(expected: String, p: KProperty1<*, *>) {
    var s = p.toString()

    // Strip "konst" or "var"
    assert(s.startsWith("konst ") || s.startsWith("var ")) { "Fail konst/var: $s" }
    s = s.substring(4)

    // Strip property type
    s = s.substringBeforeLast(':')

    // Strip property name, leave only receiver class
    s = s.substringBeforeLast('.')

    assertEquals(expected, s)
}

konst Boolean.x: Any get() = this
konst Char.x: Any get() = this
konst Byte.x: Any get() = this
konst Short.x: Any get() = this
konst Int.x: Any get() = this
konst Float.x: Any get() = this
konst Long.x: Any get() = this
konst Double.x: Any get() = this

konst BooleanArray.x: Any get() = this
konst CharArray.x: Any get() = this
konst ByteArray.x: Any get() = this
konst ShortArray.x: Any get() = this
konst IntArray.x: Any get() = this
konst FloatArray.x: Any get() = this
konst LongArray.x: Any get() = this
konst DoubleArray.x: Any get() = this

konst Array<Int>.a1: Any get() = this
konst Array<Any>.a2: Any get() = this
konst Array<Array<String>>.a3: Any get() = this
konst Array<BooleanArray>.a4: Any get() = this

konst Any?.n1: Any get() = Any()
konst Int?.n2: Any get() = Any()
konst Array<Any>?.n3: Any get() = Any()
konst Array<Any?>.n4: Any get() = Any()
konst Array<Any?>?.n5: Any get() = Any()

konst Map<String, Runnable>.m: Any get() = this
konst List<MutableSet<Array<CharSequence>>>.l: Any get() = this

fun box(): String {
    check("kotlin.Boolean", Boolean::x)
    check("kotlin.Char", Char::x)
    check("kotlin.Byte", Byte::x)
    check("kotlin.Short", Short::x)
    check("kotlin.Int", Int::x)
    check("kotlin.Float", Float::x)
    check("kotlin.Long", Long::x)
    check("kotlin.Double", Double::x)

    check("kotlin.BooleanArray", BooleanArray::x)
    check("kotlin.CharArray", CharArray::x)
    check("kotlin.ByteArray", ByteArray::x)
    check("kotlin.ShortArray", ShortArray::x)
    check("kotlin.IntArray", IntArray::x)
    check("kotlin.FloatArray", FloatArray::x)
    check("kotlin.LongArray", LongArray::x)
    check("kotlin.DoubleArray", DoubleArray::x)

    check("kotlin.Any?", Any?::n1)
    check("kotlin.Int?", Int?::n2)
    check("kotlin.Array<kotlin.Any>?", Array<Any>?::n3)
    check("kotlin.Array<kotlin.Any?>", Array<Any?>::n4)
    check("kotlin.Array<kotlin.Any?>?", Array<Any?>?::n5)

    check("kotlin.Array<kotlin.Int>", Array<Int>::a1)
    check("kotlin.Array<kotlin.Any>", Array<Any>::a2)
    check("kotlin.Array<kotlin.Array<kotlin.String>>", Array<Array<String>>::a3)
    check("kotlin.Array<kotlin.BooleanArray>", Array<BooleanArray>::a4)

    check("kotlin.collections.Map<kotlin.String, java.lang.Runnable>", Map<String, Runnable>::m)
    check("kotlin.collections.List<kotlin.collections.MutableSet<kotlin.Array<kotlin.CharSequence>>>", List<MutableSet<Array<CharSequence>>>::l)

    return "OK"
}
