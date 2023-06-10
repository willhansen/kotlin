// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

import kotlin.reflect.full.*

class C {
    fun foo() {}
    konst bar = 42
}

konst C_fooReflect = C::class.functions.find { it.name == "foo" }!!
konst C_foo = C::foo
konst cFoo = C()::foo

konst C_barReflect = C::class.memberProperties.find { it.name == "bar" }!!
konst C_bar = C::bar
konst cBar= C()::bar

konst Any.className: String
        get() = this::class.qualifiedName!!

fun box(): String =
        when {
            C_fooReflect != C_foo -> "C_fooReflect != C_foo, ${C_fooReflect.className}"
            C_foo != C_fooReflect -> "C_foo != C_fooReflect, ${C_foo.className}"
            C_fooReflect == cFoo -> "C_fooReflect == cFoo, ${C_fooReflect.className}"
            cFoo == C_fooReflect -> "cFoo == C_fooReflect, ${cFoo.className}"
            C_barReflect != C_bar -> "C_barReflect != C_bar, ${C_barReflect.className}"
            C_bar != C_barReflect -> "C_bar != C_barReflect, ${C_bar.className}"
            C_barReflect == cBar -> "C_barReflect == cBar, ${C_barReflect.className}"
            cBar == C_barReflect -> "cBar == C_barReflect, ${cBar.className}"
            else -> "OK"
        }
