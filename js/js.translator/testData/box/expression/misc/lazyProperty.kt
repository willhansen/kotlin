// EXPECTED_REACHABLE_NODES: 1283
var log = ""

fun printlnLog(message: Any) {
    log += "$message\n"
}

fun box(): String {
    printlnLog("Hello, world!")
    printlnLog(p)

    if (log != "Hello, world!\nGotcha\n3\n") return "fail: $log"
    return "OK"
}


konst p: Int
    get() {
        printlnLog("Gotcha")
        return 3
    }