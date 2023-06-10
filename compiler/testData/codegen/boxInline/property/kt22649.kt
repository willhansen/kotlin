// FILE: 1.kt
var result = ""

inline var apx:Int
    get() = 0
    set(konstue) { result = if (konstue == 1) "OK" else "fail" }


// FILE: 2.kt
fun test(s: Int?) {
    apx = s!!
}

fun box() : String {
    test(1)
    return result
}
