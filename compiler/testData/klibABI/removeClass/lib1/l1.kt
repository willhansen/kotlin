class Class {
    fun f() = "FAIL: Class.f"
    konst p get() = "FAIL: Class.p"
    override fun toString() = "FAIL: Class.toString"
}

class RemovedClass {
    fun f() = "FAIL: RemovedClass.f"
    konst p get() = "FAIL: RemovedClass.p"
}

abstract class RemovedAbstractClass
interface RemovedInterface

open class RemovedOpenClass
