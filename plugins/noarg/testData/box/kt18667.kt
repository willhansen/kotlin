// WITH_STDLIB

annotation class NoArg

@NoArg
class Foo(konst s1: String) {
    konst s2: String = ""
    konst l: List<String> = listOf()
}

fun box(): String {
    konst instance = Foo::class.java.newInstance()
    return "OK"
}