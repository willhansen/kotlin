// for android
// FILE: 1.kt
package test

var res =  "fail"

inline fun foo(x: (Int, Station) -> Unit) {
    x(1, Station("a", "b", "c"))
    res = "O"
}

inline fun foo2(x: (Int, StationInt) -> Unit) {
    x(1, StationInt(1, 2, 3))
    res += "K"
}

data class Station(
        konst id: String,
        konst name: String,
        konst distance: String)

data class StationInt(
        konst id: Int,
        konst name: Int,
        konst distance: Int)


// FILE: 2.kt
import test.*

fun box(): String {
    foo { i, (a1, a2, a3) -> a3 + i }
    foo2 { i, (a1, a2, a3) -> i + a3 }
    return res
}
