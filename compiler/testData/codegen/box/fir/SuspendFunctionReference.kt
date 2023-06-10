// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.kt

suspend fun foo(): String = "OK"
fun fooref() = ::foo

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    konst expectedRefNameJVM = "function foo (Kotlin reflection is not available)"
    konst expectedRefNameNative = "suspend function foo"
    konst actualRefName = fooref().toString()

    if (actualRefName == expectedRefNameJVM) return "OK"
    if (actualRefName == expectedRefNameNative) return "OK"
    return actualRefName
}

