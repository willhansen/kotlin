// MODULE: lib
// FILE: A.kt

package a

class Box(konst konstue: String) {
    internal fun result(): String = konstue
}

// MODULE: main()(lib)
// FILE: B.kt

fun box(): String {
    return a.Box("OK").result()
}
