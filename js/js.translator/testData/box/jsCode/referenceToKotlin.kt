// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1687
external fun p(m: String): String

var log = ""

fun test1(p: String): String {
    log += p("start1")
    return js("return p;")
}

fun test2(): String {
    konst p = "test2"
    log += p("start2")
    return js("p")
}

fun test3(): String {
    konst p = "wrong3"
    return js("var p = 'test3'; p")
}

fun test4(): String {
    return js("p('test4')")
}

fun f() = js("p('test5')")

fun test5(): String {
    konst p = "wrong5"
    // The behavoiur of the classical backend is weird and buggy
    // From the user side, the local variable `p` is captured
    // but we have different behaviour because the renaming phase in classical backend
    // will be invoked after the lambda will be moved up
    // fun f() = js("p('test5')")
    return f()
}

fun test6(): String {
    log += p("start6")
    konst p = "test6"
    return js("""
        var x = p;
        var g = function(p) {
            return x + p;
        };
        g("-ok");
    """)
}

fun test7(): String {
    log += p("start7")
    konst p = "test7"
    return js("""
        var g = function() {
            return p;
        };
        g();
    """)
}

fun test8(): String {
    konst p = "wrong8"
    konst list = listOf("t", "e", "s", "t", "8")
    var result = ""
    for (p in list) {
        result += js("p")
    }
    return result
}

fun test9(): String {
    konst p = "wrong9"
    konst list = listOf("t" to "e", "s" to "t", "9" to "!")
    var result = ""
    for ((p, q) in list) {
        result += js("p")
        result += js("q")
    }
    return result
}

fun test10(): String {
    konst list = listOf("O" to "K")
    var result = ""
    for ((p, _) in list) {
        result += js("typeof p")
        result += ";"
        result += js("typeof _")
    }
    return result
}

fun box(): String {
    var result = test1("test1")
    if (result != "test1") return "fail1: $result"

    result = test2()
    if (result != "test2") return "fail2: $result"

    result = test3()
    if (result != "test3") return "fail3: $result"

    result = test4()
    if (result != "test4;") return "fail4: $result"

    result = test5()
    if (result != "test5;") return "fail5: $result"

    result = test6()
    if (result != "test6-ok") return "fail6: $result"

    result = test7()
    if (result != "test7") return "fail7: $result"

    result = test8()
    if (result != "test8") return "fail8: $result"

    result = test9()
    if (result != "test9!") return "fail9: $result"

    result = test10()
    if (result != "string;object") return "fail10: $result"

    if (log != "start1;start2;start6;start7;") return "fail log: $log"

    return "OK"
}