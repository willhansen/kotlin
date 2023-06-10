// FILE: 1.kt

var result = "Fail"
konst unused by c

fun box(): String = result

// FILE: 2.kt

class C {
    init {
        result = "OK"
    }
}

operator fun C.getValue(x: Any?, y: Any?): String = throw IllegalStateException()

konst c = C()
