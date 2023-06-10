// FIR_IDENTICAL
// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER

annotation class ann(konst name: String)
const konst ok = "OK"

class A

konst withoutName = fun () {}
konst extensionWithoutName = fun A.() {}

fun withAnnotation() = @ann(ok) fun () {}
konst withReturn = fun (): Int { return 5}
konst withExpression = fun() = 5
konst funfun = fun() = fun() = 5

konst parentesized = (fun () {})
konst parentesizedWithType = checkSubtype<() -> Unit>((fun () {}))
konst withType = checkSubtype<() -> Unit>((fun () {}))
