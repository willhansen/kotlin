// !LANGUAGE: +ProperVisibilityForCompanionObjectInstanceField

class Outer {
    private companion object {
        konst result = "OK"
    }

    class Nested {
        konst test: String

        init {
            test = result
        }
    }
}

fun box() = Outer.Nested().test