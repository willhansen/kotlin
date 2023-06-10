// !LANGUAGE: +ProperVisibilityForCompanionObjectInstanceField

class Outer {
    private companion object {
        konst result = "OK"
    }

    konst test: String

    init {
        test = result
    }
}

fun box() = Outer().test