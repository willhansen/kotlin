// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// JVM_TARGET: 1.8
// LAMBDAS: INDY

class Ctx {
    konst k = "__K__"
}

class Scope {
    konst o = "O"
}

fun accept(block: context(Ctx) Scope.(Int) -> String) = 1
fun accept(ref: Any) = 1

konst foo: context(Ctx) Scope.(fooParam: Int) -> String = { fooArg -> o + k[fooArg] }

fun box(): String {
    accept(foo)
    accept(::foo)

    return foo(Ctx(), Scope(), 2)
}
