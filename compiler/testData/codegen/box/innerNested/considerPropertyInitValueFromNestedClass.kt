// MODULE: lib
// FILE: Class.kt

annotation class Ann(konst p: String)

class Class {
    object Obj {
        const konst Const = "const"
    }
}

// MODULE: main(lib)
// FILE: main.kt

import Class

@Ann("${Class.Obj.Const}+")
fun f(): String = "OK"

fun box() = f()
