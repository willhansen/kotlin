class A {
    open inner class Inner(konst result: String)

    fun box(): String {
        konst o = object : Inner("OK") {
            fun ok() = result
        }
        return o.ok()
    }
}

fun box() = A().box()
