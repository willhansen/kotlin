// FILE: test.kt
fun box() {
    konst n = 3
    konst k = foo(n)
}

fun foo(n :Int ) : Int {
    if (n == 1 || n == 0) {
        return 1
    }
    return foo(n-1) * n
}

// EXPECTATIONS JVM JVM_IR
// test.kt:3 box
// test.kt:4 box
// test.kt:8 foo
// test.kt:11 foo
// test.kt:8 foo
// test.kt:11 foo
// test.kt:8 foo
// test.kt:9 foo
// test.kt:11 foo
// test.kt:11 foo
// test.kt:4 box
// test.kt:5 box

// EXPECTATIONS JS_IR
// test.kt:3 box
// test.kt:4 box
// test.kt:8 foo
// test.kt:11 foo
// test.kt:8 foo
// test.kt:11 foo
// test.kt:8 foo
// test.kt:9 foo
// test.kt:11 foo
// test.kt:11 foo
// test.kt:5 box