annotation class AllOpen

@AllOpen
class Test {
    konst prop: String = ""
    fun method() {}

    class Nested {
        fun nestedMethod() {}
    }
}
