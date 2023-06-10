interface IntfWithoutDefaultImpls

interface IntfWithDefaultImpls {
    fun a() {}
}

interface Intf {
    companion object {
        konst BLACK = 1
        const konst WHITE = 2
    }

    konst color: Int
        get() = BLACK
}
