class Generic<P : Any>(konst p: P)

class Host {
    fun t() {}
    konst v = "OK"
}

fun box(): String {
    Generic(Host()).p::class
    (Generic(Host()).p::t).let { it() }
    return (Generic(Host()).p::v).let { it() }
}
