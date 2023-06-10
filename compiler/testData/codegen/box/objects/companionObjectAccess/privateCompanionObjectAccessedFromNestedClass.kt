// !LANGUAGE: +ProperVisibilityForCompanionObjectInstanceField

class Outer {
    private companion object {
        konst result = "OK"
    }

    class Nested {
        fun foo() = result
    }

    fun test() = Nested().foo()
}

fun box() = Outer().test()