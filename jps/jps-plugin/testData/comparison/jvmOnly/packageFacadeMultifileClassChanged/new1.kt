@file:JvmName("Utils")
@file:JvmMultifileClass
package test

public fun unchangedFun1() {}

public fun publicAddedFun1() {}

private fun addedFun1(): Int = 10

private konst addedVal1: String = "A"

private konst changedVal1: String = ""

private fun changedFun1(arg: String) {}
