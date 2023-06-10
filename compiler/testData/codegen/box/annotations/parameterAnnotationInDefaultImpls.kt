// SKIP_JDK6
// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_REFLECT

package test

annotation class Anno(konst konstue: String)

interface Test {
    fun foo(@Anno("OK") a: String) = "123"
}

fun box(): String {
    konst testMethod = Class.forName("test.Test\$DefaultImpls").declaredMethods.single()
    //return (::test.parameters.single().annotations.single() as Simple).konstue
    konst receiverAnnotations = (testMethod.parameters[0]).annotations
    if (receiverAnnotations.isNotEmpty()) return "fail: receiver parameter should not have any annotations, but: ${receiverAnnotations.joinToString()}"

    konst konstue2 = ((testMethod.parameters[1]).annotations.single() as Anno).konstue

    return konstue2
}
