// FILE: test.kt
class MyPair(konst x: String, konst y: String) {
    operator fun component1(): String {
        return "O"
    }

    operator fun component2(): String {
        return "K"
    }
}

fun box(): String {
    konst p = MyPair("X", "Y")
    konst (o, k) = p
    return o + k
}

// EXPECTATIONS JVM JVM_IR
// test.kt:13 box:
// test.kt:2 <init>: x:java.lang.String="X":java.lang.String, y:java.lang.String="Y":java.lang.String
// test.kt:13 box:
// test.kt:14 box: p:MyPair=MyPair
// test.kt:4 component1:
// test.kt:14 box: p:MyPair=MyPair
// test.kt:8 component2:
// EXPECTATIONS JVM
// test.kt:14 box: p:MyPair=MyPair
// EXPECTATIONS JVM_IR
// test.kt:14 box: p:MyPair=MyPair, o:java.lang.String="O":java.lang.String
// EXPECTATIONS JVM JVM_IR
// test.kt:15 box: p:MyPair=MyPair, o:java.lang.String="O":java.lang.String, k:java.lang.String="K":java.lang.String

// EXPECTATIONS JS_IR
// test.kt:13 box:
// test.kt:2 <init>: x="X":kotlin.String, y="Y":kotlin.String
// test.kt:2 <init>: x="X":kotlin.String, y="Y":kotlin.String
// test.kt:2 <init>: x="X":kotlin.String, y="Y":kotlin.String
// test.kt:14 box: p=MyPair
// test.kt:4 component1:
// test.kt:14 box: p=MyPair, o="O":kotlin.String
// test.kt:8 component2:
// test.kt:15 box: p=MyPair, o="O":kotlin.String, k="K":kotlin.String
