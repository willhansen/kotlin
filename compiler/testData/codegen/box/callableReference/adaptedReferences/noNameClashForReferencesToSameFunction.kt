
var result = ""

class C(konst token: String) {
    fun target(): Int {
        result += token
        return 42
    }
}

fun adapt(f: () -> Unit): Unit = f()

fun overload() {
    adapt(C("O")::target)
}

fun overload(unused: String) {
    adapt(C("K")::target)
}

fun box(): String {
    overload()
    overload("")
    return result
}
