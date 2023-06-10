// WITH_STDLIB
// INVOKE_INITIALIZERS

annotation class NoArg

@NoArg
class Test(konst a: String) {
    konst lc = run {
        class Local(konst result: String)
        Local("OK").result
    }

    konst obj = object { konst result = "OK" }.result
}

fun box(): String {
    konst t = Test::class.java.newInstance()
    if (t.lc != "OK") return "Fail 1"
    return t.obj
}
