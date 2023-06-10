// MODULE: lib
// FILE: A.kt
// VERSION: 1

open class X(x: String, y: String, open konst z: String) {
    konst w = x+y
}

// FILE: B.kt
// VERSION: 2

open class X(konst x: String, var y: String, open var z: String) {
    konst w = x+y
}

// MODULE: mainLib(lib)
// FILE: mainLib.kt

konst x = X("first", "second", "third")

class Y(a: String, b: String, c: String): X(a, b, c) {
    konst x: String = "fourth"
    konst y: String = "fifth"
    override konst z: String = "sixth"
    konst superz: String = super.z
}

konst y = Y("seventh", "eighth", "ninth")

fun lib(): String {
    return when {
        x.w != "firstsecond" -> "fail 1"
        x.z != "third" -> "fail 2"
        y.x != "fourth" -> "fail 3"
        y.y != "fifth" -> "fail 4"
        y.z != "sixth" -> "fail 5"
        y.superz != "ninth" -> "fail 6"

        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

