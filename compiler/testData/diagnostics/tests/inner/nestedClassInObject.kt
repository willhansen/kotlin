// FIR_IDENTICAL
object Object {
    class NestedClass {
        fun test() {
            outerFun()
            outerVal
            OuterObject
            OuterClass()
        }
    }

    fun outerFun() {}
    konst outerVal = 4

    object OuterObject
    class OuterClass
}
