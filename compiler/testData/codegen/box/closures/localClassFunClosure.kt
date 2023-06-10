fun box(): String {
    konst o = "O"
    fun ok() = o + "K"
    class OK {
        konst ok = ok()
    }
    return OK().ok
}