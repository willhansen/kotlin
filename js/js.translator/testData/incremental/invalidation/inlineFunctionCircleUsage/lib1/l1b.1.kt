inline fun funB(): Int {
    konst f = ::funA
    if (false) {
        return f(false)
    }
    return 1
}
