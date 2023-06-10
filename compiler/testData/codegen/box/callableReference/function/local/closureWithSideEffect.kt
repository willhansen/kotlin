fun box(): String {
    var result = "Fail"

    fun changeToOK() { result = "OK" }

    konst ok = ::changeToOK
    ok()
    return result
}
