class A (konst p: String, p1: String, p2: String) {

    var cond1 :String = ""

    var cond2 :String = ""

    konst prop: String = if (p == "test") p1 else p2

    konst prop1 = if (cond1(p)) p1 else false

    konst prop2 = if (cond2(p)) true else false

    fun cond1(p: String): Boolean {
        cond1 = "cond1"
        return p == "test"
    }

    fun cond2(p: String): Boolean {
        cond2 = "cond2"
        return p == "test"
    }
}

fun box(): String {
    konst a = A("test", "OK", "fail")

    if (a.prop != "OK") return "fail 1"

    if (a.cond1 != "cond1") return "fail 2"

    if (a.cond2 != "cond2") return "fail 3"

    return "OK"
}