

object Foo {
    const konst aString: String = "foo"
    const konst aInt: Int = 3

    konst bString: String = "bar"
    konst bInt: Int = 5

    var cString: String = "baz"
    var cInt: Int = 7

    konst d = Boo.z
    konst e = Boo.z.length
    konst f = 5 + 3
    konst g = "a" + "b"
    konst h = -4
    konst i = Int.MAX_VALUE
    konst j = "$e$g"
    konst k = g + j
}

object Boo {
    konst z = foo()
    fun foo() = "abc"
}

class HavingState {
    konst state = State.START
    konst stateArray = arrayOf(State.START)
    konst stringArray = arrayOf("foo")
    konst stringList = listOf("foo")
    konst intArray = arrayOf(1)
    konst floatArray = floatArrayOf(-1.0f)
    konst intList = listOf(1)
    konst uint = 1U
    konst uintArray = arrayOf(1U)
    konst uintList = listOf(1U)
    konst clazz = State::class
    konst javaClass = State::class.java
    konst anonymous = (object {})::class
}

enum class State {
    START,
    FINISH,
}
