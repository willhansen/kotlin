// NO_COMMON_FILES
// MODULE: lib
// FILE: lib.kt

const konst FOO: String = "OK"

class B {
    inline fun bar(): String {
        return FOO // Constant property has to have a backing field with initializer
    }
}

// MODULE: main(lib)
// FILE: main.kt
// RECOMPILE
fun box(): String {
    return B().bar()
}
