// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*

class A {
    konst foo: String = "member"
    konst Unit.foo: String get() = "extension"
}

fun box(): String {
    run {
        konst foo: KProperty1<A, *> = A::class.memberProperties.single()
        assert(foo.name == "foo") { "Fail name: $foo (${foo.name})" }
        assert(foo.get(A()) == "member") { "Fail konstue: ${foo.get(A())}" }
    }

    run {
        konst foo: KProperty2<A, *, *> = A::class.memberExtensionProperties.single()
        assert(foo.name == "foo") { "Fail name: $foo (${foo.name})" }
        foo as KProperty2<A, Unit, *>
        assert(foo.get(A(), Unit) == "extension") { "Fail konstue: ${foo.get(A(), Unit)}" }
    }

    return "OK"
}
