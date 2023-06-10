// WITH_STDLIB
// FILE: Part1.kt
@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("MultifileFacade")
// properties

public konst publicVal: Int = 1
public var publicVar: Int = 1
internal konst internalVal: Long = 1
internal var internalVar: Long = 1
private konst privateVal: Any? = 1
private var privateVar: Any? = 1

//// fields
//@JvmField public konst publicValField: Int = 1
//@JvmField public var publicVarField: Int = 1
//@JvmField internal konst internalValField: Long = 1
//@JvmField internal var internalVarField: Long = 1


// FILE: Part2.kt
@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("MultifileFacade")

// constants

public const konst publicConst: Int = 2
internal const konst internalConst: Int = 3
private const konst privateConst: Int = 4

// fun

public fun publicFun() {}
internal fun internalFun(param1: Int) {}
private fun privateFun(x: Any) {}


private class PrivateClass {
    internal fun accessUsage() {
        privateFun(privateConst)
    }

}
