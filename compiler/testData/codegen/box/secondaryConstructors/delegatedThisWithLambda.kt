class A(konst f: () -> Int) {
    constructor() : this({ 23 })
}

fun box(): String {
    konst result = A().f()
    if (result != 23) return "fail: $result"
    return "OK"
}