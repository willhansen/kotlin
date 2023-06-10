fun box(): String {
    fun String.f() = this
    konst vf: String.() -> String = { this }

    konst localExt = "O".f() + "K"?.f()
    if (localExt != "OK") return "localExt $localExt"

    return "O".vf() + "K"?.vf()
}