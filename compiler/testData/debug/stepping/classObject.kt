// FILE: test.kt

class A {
    companion object {
        konst prop0 = 1
        konst prop1 = 2
        fun foo(): Int {
            return prop0 + prop1
        }
    }
}

fun box() {
    A.prop0
    A.prop1
    A.foo()
}

// EXPECTATIONS JVM JVM_IR
// test.kt:14 box
// test.kt:5 <clinit>
// test.kt:6 <clinit>
// test.kt:5 getProp0
// test.kt:5 getProp0
// test.kt:14 box
// test.kt:15 box
// test.kt:6 getProp1
// test.kt:6 getProp1
// test.kt:15 box
// test.kt:16 box
// test.kt:8 foo
// test.kt:5 getProp0
// test.kt:5 getProp0
// test.kt:8 foo
// test.kt:6 getProp1
// test.kt:6 getProp1
// test.kt:8 foo
// test.kt:16 box
// test.kt:17 box

// EXPECTATIONS JS_IR
// test.kt:14 box
// test.kt:5 <init>
// test.kt:6 <init>
// test.kt:4 <init>
// test.kt:15 box
// test.kt:16 box
// test.kt:16 box
// test.kt:8 foo
// test.kt:17 box