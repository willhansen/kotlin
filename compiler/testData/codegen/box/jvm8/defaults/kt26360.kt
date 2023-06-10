// JVM_TARGET: 1.8
// !JVM_DEFAULT_MODE: all
// WITH_STDLIB
// TARGET_BACKEND: JVM
interface Base {
    fun konstue(): String
}

interface SubA : Base

@JvmDefaultWithCompatibility
interface SubB : Base {
    override fun konstue(): String = "OK"
}

interface SubAB : SubA, SubB

fun box(): String {
    return object : SubAB {}.konstue()
}
