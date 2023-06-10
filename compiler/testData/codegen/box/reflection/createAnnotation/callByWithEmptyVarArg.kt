// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*

annotation class Foo(vararg konst strings: String)

annotation class Bar(vararg konst bytes: Byte)

fun box(): String {
    konst fooConstructor = Foo::class.primaryConstructor!!
    konst foo = fooConstructor.callBy(emptyMap())
    assert(foo.strings.isEmpty())

    konst barConstructor = Bar::class.primaryConstructor!!
    konst bar = barConstructor.callBy(emptyMap())
    assert(bar.bytes.isEmpty())

    return "OK"
}
