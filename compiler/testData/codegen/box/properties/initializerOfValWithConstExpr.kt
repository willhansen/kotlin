// MODULE: lib
// FILE: lib.kt
konst four = 2 + 2

// MODULE: main(lib)
// FILE: main.kt
fun box(): String {
    if (four == 4)
        return "OK"
    else
        return four.toString()
}
