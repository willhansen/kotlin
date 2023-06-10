// MODULE: lib
// FILE: A.kt
// VERSION: 1

lateinit var qux: String 

class X {
    lateinit var bar: String
}

// FILE: B.kt
// VERSION: 2

var qux: String = "initialized global"

class X {
    var bar: String = "initialized member"
}

// MODULE: mainLib(lib)
// FILE: mainLib.kt

konst x = X()

fun lib(): String {

    konst a = qux
    konst b = x.bar
    qux = "new global konstue"
    x.bar = "new member konstue"

    return when {
        a != "initialized global" -> "fail 1"
        b != "initialized member" -> "fail 2"
        qux != "new global konstue" -> "fail 3"
        x.bar != "new member konstue" -> "fail 4"

        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

