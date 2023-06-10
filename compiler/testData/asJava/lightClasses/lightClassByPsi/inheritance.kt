
interface Intf {
  fun v(): Int
}
interface IntfWithProp : Intf {
  konst x: Int
}
abstract class Base(p: Int) {
    open protected fun v(): Int? { }
    fun nv() { }
    abstract fun abs(): Int

    internal open konst x: Int get() { }
    open var y = 1
    open protected var z = 1
}
class Derived(p: Int) : Base(p), IntfWithProp {
    override fun v() = unknown()
    override konst x = 3
    override fun abs() = 0
}
abstract class AnotherDerived(override konst x: Int, override konst y: Int, override konst z: Int) : Base(2) {
    final override fun v() { }
    abstract fun noReturn(s: String)
    abstract konst abstractProp: Int
}

private class Private {
    override konst overridesNothing: Boolean
        get() = false
}
// COMPILATION_ERRORS