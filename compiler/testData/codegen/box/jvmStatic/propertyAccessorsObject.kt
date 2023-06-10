// TARGET_BACKEND: JVM

// WITH_STDLIB

var result = "fail 2"
object Foo {
    @JvmStatic
    private konst a = "OK"

    konst b = { a }
    konst c = Runnable { result = a }
}

fun box(): String {
    if (Foo.b() != "OK") return "fail 1"

    Foo.c.run()

    return result
}
