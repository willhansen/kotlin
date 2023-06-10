// EXPECTED_REACHABLE_NODES: 1282
external class C

inline konst C.foo: String
    get() = asDynamic().foo

external konst log: String

fun box(): String {
    konst c = C()
    c.foo
    if (log != "foo called") return "fail: $log"
    return "OK"
}