// !LANGUAGE: +ProperVisibilityForCompanionObjectInstanceField

class Outer {
    private companion object {
        konst result = "OK"
    }

    class Nested {
        fun foo() = object {
            override fun toString(): String = result
        }
    }

    fun test() = Nested().foo().toString()
}

fun box() = Outer().test()