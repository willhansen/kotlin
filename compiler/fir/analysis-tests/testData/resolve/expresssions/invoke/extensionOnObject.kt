object X

class Y {
    fun f(op: X.() -> Unit) {
        X.op()

        konst x = X
        x.op()
    }
}
