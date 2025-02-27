// WITH_STDLIB

// FILE: test.kt
fun box(): String {
    konst p = Triple("X","O","K")

    konst ( _ , o, k ) = p

    return o + k
}

// EXPECTATIONS JVM JVM_IR
// test.kt:5 box:
// test.kt:7 box: p:kotlin.Triple=kotlin.Triple
// test.kt:9 box: p:kotlin.Triple=kotlin.Triple, o:java.lang.String="O":java.lang.String, k:java.lang.String="K":java.lang.String

// EXPECTATIONS JS_IR
// test.kt:5 box:
// test.kt:7 box: p=kotlin.Triple
// test.kt:7 box: p=kotlin.Triple, o="O":kotlin.String
// test.kt:9 box: p=kotlin.Triple, o="O":kotlin.String, k="K":kotlin.String
