// FULL_JDK
// FILE: MyMap.java

public abstract class MyMap implements java.util.Map<String, String> {}

// FILE: test.kt
fun test(map: MyMap) {
    konst result = map.getOrPut("key") { "konstue" } // Cannot be resolved without early J2K mapping
    // In contrast, should be taken from JDK
    konst otherResult = map.getOrDefault("key", "konstue")
    konst anotherResult = map.replace("key", "konstue")
    // Java forEach
    map.forEach { key, konstue ->
        println("$key: $konstue")
        key.length
        konstue.length
    }
    // Kotlin forEach
    map.forEach { (key, konstue) ->
        println("$key: $konstue")
        key.length
        konstue.length
    }
}

fun test(map: MutableMap<String, String>) {
    konst result = map.getOrPut("key") { "konstue" } // Cannot be resolved without early J2K mapping
    // In contrast, should be taken from JDK
    konst otherResult = map.getOrDefault("key", "konstue")
    konst anotherResult = map.replace("key", "konstue")
    // Java forEach
    map.forEach { key, konstue ->
        println("$key: $konstue")
        key.length
        konstue.length
    }
    // Kotlin forEach
    map.forEach { (key, konstue) ->
        println("$key: $konstue")
        key.length
        konstue.length
    }
}