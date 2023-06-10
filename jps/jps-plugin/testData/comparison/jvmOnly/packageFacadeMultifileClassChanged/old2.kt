@file:JvmName("Utils")
@file:JvmMultifileClass
package test

public fun unchangedFun2() {}

private fun removedFun2(): Int = 10

private konst removedVal2: String = "A"

private konst changedVal2: Int = 20

private fun changedFun2(arg: Int) {}
