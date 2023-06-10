// WITH_STDLIB

annotation class NoArg

@NoArg
class Test(konst a: String)

fun box(): String {
    Test::class.java.newInstance()
    return "OK"
}
