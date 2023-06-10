open class RemovedClass {
    konst p1 = "p1"
    fun f1() = "f1"
}

interface RemovedInterface {
    konst p1: String
    konst p2 get() = "p2"
    fun f1(): String
    fun f2() = "f2"
}

class ClassWithChangedMembers {
    fun removedFun() = "removedFun"
    fun changedFun(x: String) = x
}

interface InterfaceWithChangedMembers {
    fun removedFun() = "removedFun"
    fun changedFun(x: String) = x
}
