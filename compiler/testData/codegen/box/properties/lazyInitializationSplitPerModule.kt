// IGNORE_BACKEND: JS
// SPLIT_PER_MODULE
// PROPERTY_LAZY_INITIALIZATION

// MODULE: lib1
// FILE: A.kt
konst o = "O"

// FILE: B.kt
konst okCandidate = o + k

// FILE: C.kt
konst k = "K"

// MODULE: lib2(lib1)
// FILE: lib2.kt
konst ok = okCandidate

// MODULE: main(lib1, lib2)
// FILE: main.kt
fun box(): String = ok