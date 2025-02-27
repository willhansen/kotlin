// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: MINOR: JS_NAME_CLASH
// IGNORE_BACKEND_K1: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS

class Foo(konst s: String)
fun foo(): Foo? = Foo("OK")

fun <T> run(f: () -> T): T = f()

konst foo: Foo = run {
    konst x = foo()
    if (x == null) throw Exception()
    x
}

fun box() = foo.s
