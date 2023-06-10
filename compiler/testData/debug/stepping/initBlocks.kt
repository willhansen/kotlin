// FILE: test.kt

class Foo {
    var a: String

    init {
        a = x()
    }
}

class Bar {
    init {
        konst a = 5
    }

    init {
        konst b = 2
    }
}

class Boo {
    init {
        konst a = 5
    }

    konst x = x()

    init {
        konst b = 2
    }
}

class Zoo {
    init { konst a = 5 }

    init { konst b = 6 }

    init {
        konst c = 7
    }

    init { konst d = 8 }
}

fun x() = ""

fun box() {
    Foo()
    Bar()
    Boo()
    Zoo()
}

// JVM_IR has an extra step back to the line of the class
// declaration for the return in the constructor.

// EXPECTATIONS JVM JVM_IR
// test.kt:48 box
// test.kt:3 <init>
// test.kt:6 <init>
// test.kt:7 <init>
// test.kt:45 x
// test.kt:7 <init>
// test.kt:8 <init>
// EXPECTATIONS JVM_IR
// test.kt:3 <init>
// EXPECTATIONS JVM JVM_IR
// test.kt:48 box
// test.kt:49 box
// test.kt:11 <init>
// test.kt:12 <init>
// test.kt:13 <init>
// test.kt:14 <init>
// test.kt:16 <init>
// test.kt:17 <init>
// test.kt:18 <init>
// EXPECTATIONS JVM_IR
// test.kt:11 <init>
// EXPECTATIONS JVM JVM_IR
// test.kt:49 box
// test.kt:50 box
// test.kt:21 <init>
// test.kt:22 <init>
// test.kt:23 <init>
// test.kt:24 <init>
// test.kt:26 <init>
// test.kt:45 x
// test.kt:26 <init>
// test.kt:28 <init>
// test.kt:29 <init>
// test.kt:30 <init>
// EXPECTATIONS JVM_IR
// test.kt:21 <init>
// EXPECTATIONS JVM JVM_IR
// test.kt:50 box
// test.kt:51 box
// test.kt:33 <init>
// test.kt:34 <init>
// test.kt:36 <init>
// test.kt:38 <init>
// test.kt:39 <init>
// test.kt:40 <init>
// test.kt:42 <init>
// EXPECTATIONS JVM_IR
// test.kt:33 <init>
// EXPECTATIONS JVM JVM_IR
// test.kt:51 box
// test.kt:52 box

// EXPECTATIONS JS_IR
// test.kt:48 box
// test.kt:7 <init>
// test.kt:45 x
// test.kt:3 <init>
// test.kt:49 box
// test.kt:13 <init>
// test.kt:17 <init>
// test.kt:11 <init>
// test.kt:50 box
// test.kt:23 <init>
// test.kt:26 <init>
// test.kt:45 x
// test.kt:29 <init>
// test.kt:21 <init>
// test.kt:51 box
// test.kt:34 <init>
// test.kt:36 <init>
// test.kt:39 <init>
// test.kt:42 <init>
// test.kt:33 <init>
// test.kt:52 box