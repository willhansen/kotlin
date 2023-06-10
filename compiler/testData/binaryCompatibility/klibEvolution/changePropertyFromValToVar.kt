// MODULE: lib
// FILE: A.kt
// VERSION: 1

konst bar = "global konst initialization konstue"

class X {
    konst qux = "member konst initialization konstue"
}

fun foo(x: X) {
    // DO NOTHING
}

// FILE: B.kt
// VERSION: 2

var bar = "global var initialization konstue"

class X {
    var qux = "member var initialization konstue"
}

fun foo(x: X) {
    bar = "changed global konstue"
    x.qux = "changed member konstue"
    
}


// MODULE: mainLib(lib)
// FILE: mainLib.kt
fun lib(): String {
    konst x = X()
    foo(x)

    return when {
        bar != "changed global konstue" -> "fail 1"
        x.qux != "changed member konstue" -> "fail 2"

        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

