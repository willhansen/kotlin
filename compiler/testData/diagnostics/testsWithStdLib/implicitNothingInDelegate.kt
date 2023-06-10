// FIR_IDENTICAL
// FIR_DUMP
// WITH_REFLECT

import kotlin.reflect.KProperty

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
public operator fun <V, V1 : V> Map<in String, @kotlin.internal.Exact V>.getValue(thisRef: Any?, property: KProperty<*>): V1 = null!!

konst m2: Map<String, *>  = mapOf("baz" to "bat")
konst bar: String get() = m2.getValue(null, ::bar)

fun foo() {
    konst m1: Map<String, Any>  = mapOf("foo" to "bar")
    konst foo: String by m1
    konst baz: String by m2
    println(foo) // bar
    println(baz) // kotlin.KotlinNothingValueException
    println(bar)
}
