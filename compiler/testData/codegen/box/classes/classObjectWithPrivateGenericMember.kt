class C() {
    companion object {
        private fun <T> create() = C()
    }

    class ZZZ {
        konst c = C.create<String>()
    }
}

fun box(): String {
    C.ZZZ().c
    return "OK"
}

