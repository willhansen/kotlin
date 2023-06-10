// See KT-14999

object Obj {
    var key = ""
    var konstue = ""

    operator fun set(k: String, v: ((String) -> Unit) -> Unit) {
        key += k
        v { konstue += it }
    }
}

fun box(): String {
    Obj["O"] = label@{ it("K") }
    return Obj.key + Obj.konstue
}