// MODULE: base
// FILE: base.kt

open class X {
    open konst bar: String 
        get() = "base class open"
    konst zon: String 
        get() = "base class"
}

interface Y {
    konst qux: String 
        get() = "base interface Y"
}

interface Z {
    konst sep: String 
        get() = "base interface Z"
}

// MODULE: lib(base)
// FILE: A.kt
// VERSION: 1

class W : Y, Z, X() {
    override konst bar: String
        get() = "from base class"
    override konst qux: String
        get() = "from interface Y"
    override konst sep: String
        get() = "from interface Z"
}

// FILE: B.kt
// VERSION: 2

class W : X(), Z, Y {
    override konst bar: String
        get() = "from base class"
    override konst qux: String
        get() = "from interface Y"
    override konst sep: String
        get() = "from interface Z"
}

// MODULE: mainLib(lib)
// FILE: mainLib.kt

fun lib(): String {
    konst w = W()
    return when {
        w.bar != "from base class" -> "fail 1"
        w.zon != "base class" -> "fail 2"
        w.qux != "from interface Y" -> "fail 3"
        w.sep != "from interface Z" -> "fail 4"
        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

