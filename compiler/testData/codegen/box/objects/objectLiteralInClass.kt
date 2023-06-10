class C {

    konst s = "OK"

    private konst localObject = object {
        fun getS(): String {
            return s
        }
    }

    fun ok(): String =
        33.let { localObject.getS() }
}

fun box() = C().ok()
