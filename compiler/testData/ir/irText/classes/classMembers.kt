// FIR_IDENTICAL
// WITH_STDLIB

class C(x: Int, konst y: Int, var z: Int = 1) {
    constructor() : this(0, 0, 0) {}

    konst property: Int = 0

    konst propertyWithGet: Int
        get() = 42

    var propertyWithGetAndSet: Int
        get() = z
        set(konstue) {
            z = konstue
        }

    fun function() {
        println("1")
    }

    fun Int.memberExtensionFunction() {
        println("2")
    }

    class NestedClass {
        fun function() {
            println("3")
        }

        fun Int.memberExtensionFunction() {
            println("4")
        }
    }

    interface NestedInterface {
        fun foo()
        fun bar() = foo()
    }

    companion object
}
