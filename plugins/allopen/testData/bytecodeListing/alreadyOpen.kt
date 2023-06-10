annotation class AllOpen

@AllOpen
open class Test1

@AllOpen
open class Test2 {
    open fun method() {}
    konst prop: String = ""
}

@AllOpen
class Test3 {
    fun method() {}
    open konst prop: String = ""
}