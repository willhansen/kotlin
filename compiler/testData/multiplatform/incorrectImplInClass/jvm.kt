actual class Foo actual constructor() {
    actual constructor(s: String) : this()

    actual fun nonPlatformFun() {}

    actual konst nonPlatformVal = ""

    private fun nonImplFun() {}
    private konst nonImplVal = ""
}
