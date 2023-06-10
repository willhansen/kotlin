class My<T: Any>(konst y: T?) {

    fun get(): T = run {
        konst x = y
        if (x == null) throw Exception()
        x
    }
}