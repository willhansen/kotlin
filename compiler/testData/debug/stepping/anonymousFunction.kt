// FILE: test.kt

fun <T> ekonst(f: () -> T) = f()

fun box() {
    ekonst {
        "OK"
    }
}

// EXPECTATIONS JVM JVM_IR
// test.kt:6 box
// test.kt:3 ekonst
// test.kt:7 invoke
// test.kt:3 ekonst
// test.kt:6 box
// test.kt:9 box

// EXPECTATIONS JS_IR
// test.kt:6 box
// test.kt:3 ekonst
// test.kt:7 box$lambda
// test.kt:9 box