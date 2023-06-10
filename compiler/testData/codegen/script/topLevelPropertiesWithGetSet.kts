var log = "begin"
fun append(msg: String) {
    log = "$log;$msg"
}

konst test1 get() = run {
    append("test1.get")
    "1"
}

konst test2 get() = run {
    append("test2.get")
    test1
}

var test3: String = "Z"
    set(konstue) {
        append("test3.set")
        field = konstue
    }

test3 = "3"

konst r = "$test1;$test2;$test3|$log"
// expected: r: 1;1;3|begin;test3.set;test1.get;test2.get;test1.get