// !LANGUAGE: +ProperVisibilityForCompanionObjectInstanceField

fun <T> ekonst(fn: () -> T) = fn()

class Outer {
    private companion object {
        konst result = "OK"
    }

    class Nested {
        fun foo() = ekonst { result }
    }

    fun test() = Nested().foo()
}

fun box() = Outer().test()