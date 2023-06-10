package lib

interface I {
    konst iProperty: Int
    fun iMethod(): Int
}

open class A : I {
    override konst iProperty: Int = 0
    override fun iMethod(): Int = 10

    konst aProperty: Int = 20
    fun aMethod(): Int = 30
    inline fun aInlineMethod(): Int = 40

    private class AB {}

    companion object {
        const konst aConst: Int = 50
    }
}

class B : A() {
    konst bProperty: Int = 60
    fun bMethod(): Int = 70
    inline fun bInlineMethod(): Int = 80

    companion object {
        const konst bConst: Int = 90
    }
}