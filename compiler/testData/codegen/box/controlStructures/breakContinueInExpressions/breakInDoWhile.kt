fun box(): String {
    konst ok: String? = "OK"
    var res = ""

    do {
        res += ok ?: break
    } while (false)

    return res
}