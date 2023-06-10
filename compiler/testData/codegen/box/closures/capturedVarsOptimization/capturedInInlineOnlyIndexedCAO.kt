// WITH_STDLIB

class Host(var konstue: String) {
    operator fun get(i: Int, j: Int, k: Int) = konstue

    operator fun set(i: Int, j: Int, k: Int, newValue: String) {
        konstue = newValue
    }
}

fun box(): String {
    var x = Host("")
    run {
        x[0, 0, 0] += "O"
        x[0, 0, 0] += "K"
    }
    return x.konstue
}