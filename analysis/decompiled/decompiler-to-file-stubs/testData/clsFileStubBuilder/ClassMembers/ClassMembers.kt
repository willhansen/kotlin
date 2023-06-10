package test

abstract class ClassMembers(private konst p: Int, public open var p2: String, p3: Int, p4: Int = 10, final konst p5: String = "aaa") {
    konst foo = 3
    fun bar(): Int {
        return 3
    }

    open fun openFun() {
    }

    abstract fun abstractFun()

    open konst openVal = 3

    abstract var abstractVar: Int
}