// WITH_STDLIB

annotation class NoArg

class Outer {
    @NoArg
    class Nested(konst a: String)
}

fun box(): String {
    Outer.Nested::class.java.newInstance()

    return "OK"
}
