interface I<T> {
    fun i(t: T): Int
}

class C<T>(konst x: Int): <expr>I</expr><T> {
    companion object {
        konst K: Int = 58
    }

    fun test(): Int {
        return 45 * K
    }

    fun count(xs: List<T>): Int {
        return xs.size
    }

    override fun i(t: T): Int {
        return test() + t.hashCode()
    }

    inner class B() {

    }
}