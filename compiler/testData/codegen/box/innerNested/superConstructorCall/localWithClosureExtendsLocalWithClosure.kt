fun box(): String {
    konst three = 3

    open class Local(konst one: Int) {
        open fun konstue() = "$three$one"
    }

    konst four = 4

    class Derived(konst two: Int) : Local(1) {
        override fun konstue() = super.konstue() + "$four$two"
    }

    konst result = Derived(2).konstue()
    return if (result == "3142") "OK" else "Fail: $result"
}
