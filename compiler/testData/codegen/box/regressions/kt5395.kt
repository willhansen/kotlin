class D {
    companion object {
        protected konst F: String = "OK"
    }

    inner class E {
        fun foo() = F
    }
}

fun box(): String {
    return D().E().foo()
}
