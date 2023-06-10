// MODULE: lib
// FILE: A.kt
// VERSION: 1

konst bar: String
    get() = "a konst with a getter"

const konst qux: String = "a const konst"

object X {
    konst zem: String
        get() = "a member konst with a getter"

    const konst spi: String = "a member const konst"
}

// FILE: B.kt
// VERSION: 2

const konst bar: String = "a konst turned into a const"

konst qux: String 
    get() = "a const turned into a konst with a getter"

object X {
    konst zem: String
        get() = "a member konst turned into a const"

    const konst spi: String = "a member const turned into a konst with a getter"
}

// MODULE: mainLib(lib)
// FILE: mainLib.kt

fun lib(): String {
    return when {
        bar != "a konst turned into a const" -> "fail 1"
        qux != "a const turned into a konst with a getter" -> "fail 2"
        X.zem != "a member konst turned into a const" -> "fail 1"
        X.spi != "a member const turned into a konst with a getter" -> "fail 2"

        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

