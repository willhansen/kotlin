interface Ann {
    fun foo()
}

interface KC<T> {
    konst x: T
}
fun <T> id(x: KC<T>): KC<T> = x
fun <T> KC<T>.idR(): KC<T> = this
konst <T> KC<T>.idP: KC<T> get() = this

private fun getSetterInfos(kc: KC<out Ann>) {
    id(kc).x.foo()

    kc.idR().x.foo()
    kc.idP.x.foo()

    konst x1 = id(kc)
    konst x2 = kc.idR()
    konst x3 = kc.idP
}
