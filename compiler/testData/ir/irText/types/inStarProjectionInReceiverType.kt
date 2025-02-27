// FIR_IDENTICAL

interface Foo<in T> {
    konst x: Int
    fun foo(x: T)
}

fun Foo<*>.testReceiver() = x

fun Foo<*>.testSmartCastOnExtensionReceiver() {
    this as Foo<String>
    foo("string")
}

fun testValueParameter(vp: Foo<*>) = vp.x
