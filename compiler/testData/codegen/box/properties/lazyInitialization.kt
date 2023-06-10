// IGNORE_BACKEND: JS
// PROPERTY_LAZY_INITIALIZATION

// FILE: A.kt

konst o = "O"

// FILE: B.kt
konst ok = o + k

// FILE: C.kt

konst k = "K"

// FILE: main.kt

fun box(): String = ok