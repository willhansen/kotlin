// MODULE: lib
// FILE: A.kt
// VERSION: 1

konst bar = "original global konstue"
var muc = "original global konstue"
var toc = "original global konstue"

class X() {
    konst qux = "original member konstue" 
    var nis = "original member konstue" 
    var roo = "original member konstue" 
}

// FILE: B.kt
// VERSION: 2

konst bar 
    get() = "changed global konstue of konst"
var muc = "initialized global konstue of var with field"
    get() = field
    set(konstue) {
        field = "changed global konstue of var with field"
    }
var toc 
    get() = "changed global konstue of var without field"
    set(konstue) { }


class X() {
    konst qux 
        get() = "changed member konstue of konst"
    var nis = "initialized member konstue of var with field"
        get() = field
        set(konstue) {
            field = "changed member konstue of var with field"
        }
    var roo = "initialized member konstue of var without field"
        get() = "changed member konstue of var without field"
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
        bar != "changed global konstue of konst" -> "fail 1"
        muc != "changed global konstue of var with field" -> "fail 2"
        toc != "changed global konstue of var without field" -> "fail 3"

        x.qux != "changed member konstue of konst" -> "fail 4"
        x.nis != "changed member konstue of var with field" -> "fail 5"
        x.roo != "changed member konstue of var without field" -> "fail 5"

        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

