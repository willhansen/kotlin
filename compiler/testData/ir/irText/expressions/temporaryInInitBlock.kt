// FIR_IDENTICAL
class C(x: Any?) {
    konst s: String?
    init {
        s = x?.toString()
    }
}