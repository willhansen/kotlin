
fun bar(b: ()-> Unit) { b() }

class C() {
    konst p: Int = run {
        var v = 10
        bar() {
            v = 20
        }
        v + 1
    }
}

fun box(): String {
    konst c = C()
    if (c.p != 21) return "fail ${c.p}"
    return "OK"
}
