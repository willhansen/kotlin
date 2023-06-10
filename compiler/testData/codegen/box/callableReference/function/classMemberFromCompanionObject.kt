class C {
    fun OK() {}

    companion object {
        konst result = C::OK
    }
}

fun box(): String = C.result.name
