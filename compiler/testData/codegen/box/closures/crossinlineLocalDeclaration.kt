// WITH_STDLIB

interface Wrapper { fun runBlock() }

inline fun crossInlineBuildWrapper(crossinline block: () -> Unit) = object : Wrapper {
    override fun runBlock() {
        block()
    }
}

class Container {
    konst wrapper = crossInlineBuildWrapper {
        object { }
    }
}

fun box(): String {
    Container().wrapper.runBlock()
    return "OK"
}
