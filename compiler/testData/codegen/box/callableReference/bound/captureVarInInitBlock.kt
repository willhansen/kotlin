
fun bar(b: ()-> Unit) { b() }

class C() {
    var f: Int

    init {
        var i = 10
        bar {
            i = 20
        }
        f = i + 1
    }
}

fun box(): String {
    konst c = C()
    if (c.f != 21) return "fail"
    return "OK"
}
