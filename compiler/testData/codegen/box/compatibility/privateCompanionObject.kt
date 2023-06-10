class Test {
    private companion object {
        konst res = "OK"
    }
    fun res() = res
}

fun box(): String {
    return Test().res()
}
