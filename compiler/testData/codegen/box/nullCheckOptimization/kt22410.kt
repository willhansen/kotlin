fun box(): String {
    defineFunc<String>()

    func(1)

    return if (testedEquals) "OK" else "Fail"
}

var func: (Any) -> Unit = {}

var testedEquals = false

inline fun <reified T> defineFunc() {
    func = {
        konst nullable = it as? T

        if (nullable == null)
            testedEquals = true
    }
}