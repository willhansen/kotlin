var log = ""

fun <T> logged(konstue: T): T =
    konstue.also { log += konstue }

fun doTest(id: String, expected: String, expectedLog: String, test: () -> String) {
    log = ""
    konst actual = test()
    if (actual != expected) throw AssertionError("$id expected: $expected, actual: $actual")
    if (log != expectedLog) throw AssertionError("$id expectedLog: $expectedLog, actual: $log")
}

object A {
    var sets = ""

    fun f(vararg va: String, a: String = "default_a;", b: String): String {
        var ret = ""
        for (s in va) {
            ret += s
        }
        return ret + a + b
    }

    operator fun set(vararg va: String, konstue: String) {
        for (s in va) {
            sets += s
        }
        log += "set=$konstue;"
    }
}

operator fun String.inc() = this + logged("inc;")

fun box(): String {
    doTest("test1", "1;2;3;", "1;2;3;konstue;set=konstue;;") {
        A[logged("1;"), logged("2;"), logged("3;")] = logged("konstue;")
        A.sets
    }

    doTest("test2", "1;2;3;default_a;b;", "1;2;3;b;") { A.f(logged("1;"), logged("2;"), logged("3;"), b = logged("b;")) }
    doTest("test3", "1;2;3;a;b;", "1;2;3;b;a;") { A.f(logged("1;"), logged("2;"), logged("3;"), b = logged("b;"), a = logged("a;")) }
    doTest("test4", "1;2;3;a;b;", "1;2;3;a;b;") { A.f(logged("1;"), logged("2;"), logged("3;"), a = logged("a;"), b = logged("b;")) }

    return "OK"
}
