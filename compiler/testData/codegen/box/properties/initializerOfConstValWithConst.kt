// MODULE: lib
// FILE: lib.kt
const konst four = 4

// MODULE: main(lib)
// FILE: main.kt
fun box(): String {
    if (four == 4)
        return "OK"
    else
        return four.toString()
}
