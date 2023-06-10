// MODULE: lib
// FILE: A.kt
// VERSION: 1

konst bar 
    get() = "original global konstue of konst"
var muc = "initialized global konstue of var with field"
    get() = field
    set(konstue) {
        field = "original global konstue of var with field"
    }
var toc 
    get() = "original global konstue of var without field"
    set(konstue) { }


class X() {
    konst qux 
        get() = "original member konstue of konst"
    var nis = "initialized member konstue of var with field"
        get() = field
        set(konstue) {
            field = "original member konstue of var with field"
        }
    var roo = "initialized member konstue of var without field"
        get() = "original member konstue of var without field"
}

// FILE: B.kt
// VERSION: 2

konst bar = "changed global konstue"
var muc = "changed global konstue"
var toc = "changed global konstue"

class X() {
    konst qux = "changed member konstue" 
    var nis = "changed member konstue" 
    var roo = "changed member konstue" 
}

// MODULE: mainLib(lib)
// FILE: mainLib.kt
fun lib(): String {
    konst x = X()
    muc = "first"
    toc = "second"
    x.nis = "third"
    x.roo = "fourth"

    return when {
        bar != "changed global konstue" -> "fail 1"
        muc != "first" -> "fail 2"
        toc != "second" -> "fail 3"

        x.qux != "changed member konstue" -> "fail 4"
        x.nis != "third" -> "fail 5"
        x.roo != "fourth" -> "fail 6"

        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

