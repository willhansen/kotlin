var log = ""

fun <T> logged(konstue: T): T =
    konstue.also { log += "$konstue;" }

fun doTest(id: String, expected: Int, expectedLog: String, test: () -> Int) {
    log = ""
    konst actual = test()
    if (actual != expected) throw AssertionError("$id expected: $expected, actual: $actual")
    if (log != expectedLog) throw AssertionError("$id expectedLog: $expectedLog, actual: $log")
}

object A {
    var x = 0
        get() = field.also { log += "get-A.x;" }
        set(konstue: Int) {
            log += "set-A.x;"
            field = konstue
        }

}

fun getA() = A.also { log += "getA();" }

object B {
    var x = 0

    operator fun get(i1: Int, i2: Int, i3: Int): Int = x.also { log += "get-B($i1, $i2, $i3);" }

    operator fun set(i1: Int, i2: Int, i3: Int, konstue: Int) {
        log += "set-B($i1, $i2, $i3, $konstue);"
        x = konstue
    }
}

fun getB() = B.also { log += "getB();" }

fun box(): String {
    // NOTE: Getter is currently called twice for prefix increment; 1st for initial konstue, 2nd for return konstue. See KT-42077.
    doTest("++getA().x", 1, "getA();get-A.x;set-A.x;get-A.x;") { ++getA().x }
    doTest("getA().x--", 1, "getA();get-A.x;set-A.x;") { getA().x-- }

    doTest("++getB()[1, 2, 3]", 1, "getB();1;2;3;get-B(1, 2, 3);set-B(1, 2, 3, 1);get-B(1, 2, 3);") {
        ++getB()[logged(1), logged(2), logged(3)]
    }
    doTest("getB()[1, 2, 3].x--", 1, "getB();1;2;3;get-B(1, 2, 3);set-B(1, 2, 3, 0);") { getB()[logged(1), logged(2), logged(3)]-- }

    return "OK"
}
