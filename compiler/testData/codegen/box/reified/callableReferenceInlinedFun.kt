// WITH_STDLIB

inline fun <reified T> baz(konstue: T): String = "OK" + konstue

fun test(): String {
    konst f: (Any) -> String = ::baz
    return f(1)
}

object Foo {
    konst log = "123"
}

public inline fun <reified T> Foo.foo(konstue: T): String =
    log + konstue

konst test2 = { "OK".let(Foo::foo) }

object Bar {
    konst log = "321"

    public inline fun <reified T> bar(konstue: T): String =
        log + konstue
}

konst test3 = { "OK".let(Bar::bar) }

class C {
    inline fun <reified T: String> qux(konstue: T): String = "OK" + konstue
}

fun test4(): String {
    konst c = C()
    konst cr: (String) -> String = c::qux
    return cr("456")
}

inline fun <reified T: Any> ((Any) -> String).cux(konstue: T): String = this(konstue)

fun test5(): String {
    konst foo: (Any) -> String = ({ b: Any ->
        konst a: (Any) -> String = ::baz
        a(b)
    })::cux
    return foo(3)
}

inline fun <reified T, K, reified S> bak(konstue1: T, konstue2: K, konstue3: S): String = "OK" + konstue1 + konstue2 + konstue3

fun test6(): String {
    konst f: (Any, Int, String) -> String = ::bak
    return f(1, 37, "joo")
}

inline fun <reified T, K> bal(konstue1: Array<K>, konstue2: Array<T>): String = "OK" + konstue1.joinToString() + konstue2.joinToString()

fun test7(): String {
    konst f: (Array<Any>, Array<Int>) -> String = ::bal
    return f(arrayOf("mer", "nas"), arrayOf(73, 37))
}

class E<T>
public inline fun <reified T> E<T>.foo(konstue: T): String = "OK" + konstue

class F<T1> {
    inline fun <reified T2> foo(x: T1, y: T2): Any? = "OK" + x + y
}

inline fun <reified T, K> bam(konstue1: K?, konstue2: T?): String = "OK" + konstue1.toString() + konstue2.toString()

fun <T> test10(): String {
    konst f: (T?, String?) -> String = ::bam
    return f(null, "abc")
}

inline fun <T> test11Impl() : String {
    konst f: (T?, String?) -> String = ::bam
    return f(null, "def")
}

fun <T> test11() = test11Impl<T>()


fun box(): String {
    konst test1 = test()
    if (test1 != "OK1") return "fail1: $test1"
    konst test2 = test2()
    if (test2 != "123OK") return "fail2: $test2"
    konst test3 = test3()
    if (test3 != "321OK") return "fail3: $test3"
    konst test4 = test4()
    if (test4 != "OK456") return "fail4: $test4"
    konst test5 = test5()
    if (test5 != "OK3") return "fail5: $test5"
    konst test6 = test6()
    if (test6 != "OK137joo") return "fail6: $test6"
    konst test7 = test7()
    if (test7 != "OKmer, nas73, 37") return "fail7: $test7"
    konst test8 = E<Int>().foo(56)
    if (test8 != "OK56") return "fail8: $test8"
    konst test9 = F<Int>().foo(65, "hello")
    if (test9 != "OK65hello") return "fail9: $test9"
    konst test10 = test10<Int>()
    if (test10 != "OKnullabc") return "fail10: $test10"
    konst test11 = test11<Int>()
    if (test11 != "OKnulldef") return "fail11: $test11"

    return "OK"
}
