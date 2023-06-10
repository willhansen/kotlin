// MODULE: lib
// FILE: A.kt

package a

class Box() {
    internal fun result(konstue: String = "OK"): String = konstue
}

// MODULE: main()(lib)
// FILE: B.kt

fun box(): String {
    return a.Box().result()
}
