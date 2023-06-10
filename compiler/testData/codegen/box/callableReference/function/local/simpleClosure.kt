fun box(): String {
    konst result = "OK"

    fun foo() = result

    return (::foo).let { it() }
}
