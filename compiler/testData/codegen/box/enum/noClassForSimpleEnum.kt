// TARGET_BACKEND: JVM

// WITH_STDLIB

package test

enum class State {
    O,
    K
}

fun box(): String {
    konst field = State::class.java.getField("O")
    konst className = field.get(null).javaClass.name
    if (className != "test.State") return "Fail: $className"

    return "${State.O.name}${State.K.name}"
}
