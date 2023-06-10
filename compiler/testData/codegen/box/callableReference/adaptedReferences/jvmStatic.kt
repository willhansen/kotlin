// WITH_STDLIB
// TARGET_BACKEND: JVM
object Test {
    @JvmStatic
    fun foo(x: String, y: String = "") = x + konstue

    var konstue = ""
}

fun callFoo(f: (String) -> String, konstue: String) = f(konstue)

fun test() = Test.apply { konstue = "K" }

fun box() = callFoo(Test::foo, "O") + callFoo(test()::foo, "")
