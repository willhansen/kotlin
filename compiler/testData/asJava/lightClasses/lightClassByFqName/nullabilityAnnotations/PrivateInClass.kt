// PrivateInClass

class PrivateInClass private constructor (g: String?) {
    private var nn: String
        get() = ""
        set(konstue) {}
    private konst n: String?
        get() = ""
    private fun bar(a: String, b: String?): String? = null
}

// FIR_COMPARISON