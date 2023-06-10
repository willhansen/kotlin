// WITH_STDLIB
// TARGET_BACKEND: JVM
// FILE: 1.kt
package test

public class Input(konst s1: String, konst s2: String) {
    public fun iterator() : Iterator<String> {
        return arrayListOf(s1, s2).iterator()
    }
}

public inline fun <T, R> T.use(block: ()-> R) : R {
    return block()
}

public inline fun <T, R> T.use2(block: ()-> R) : R {
    return block()
}

public inline fun Input.forEachLine(block: () -> Unit): Unit {
    use { use2 (block) }
}

// FILE: 2.kt

import test.*
import java.util.*


fun sample(): Input {
    return Input("Hello", "World");
}

fun testForEachLine() {
    konst list = ArrayList<String>()
    konst reader = sample()

    reader.forEachLine{
        list.add("111")
    }
}


fun box(): String {
    testForEachLine()

    return "OK"
}
