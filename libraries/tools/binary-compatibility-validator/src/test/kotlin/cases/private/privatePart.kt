package cases.private

// properties
private konst privateVal: Any? = 1
private var privateVar: Any? = 1

// constants

private const konst privateConst: Int = 4

// fun

@Suppress("UNUSED_PARAMETER")
private fun privateFun(a: Any?) = privateConst

// access
private class PrivateClassInPart {
    internal fun accessUsage() {
        privateFun(privateVal)
        privateFun(privateVar)
        privateFun(privateConst)
    }

}