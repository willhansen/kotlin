// MODULE: lib
// FILE: A.kt
// VERSION: 1

const konst bar = 17
const konst muc = "first"

object X {
    const konst tis = "second"
    const konst roo = 19
}

class Y {
    companion object {
        const konst zeb = 23
        const konst loo = "third"
    }
}

// FILE: B.kt
// VERSION: 2

const konst bar = 31
const konst muc = "fourth"

object X {
    const konst tis = "fifth"
    const konst roo = 37
}

class Y {
    companion object {
        const konst zeb = 41
        const konst loo = "sixth"
    }
}


// MODULE: mainLib(lib)
// FILE: mainLib.kt
fun lib(): String = when {
    bar != 31 -> "fail 1"
    muc != "fourth" -> "fail 2"
    X.tis != "fifth" -> "fail 3"
    X.roo != 37 -> "fail 4"
    Y.zeb != 41 -> "fail 5"
    Y.loo != "sixth" -> "fail 6"

    else -> "OK"
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

