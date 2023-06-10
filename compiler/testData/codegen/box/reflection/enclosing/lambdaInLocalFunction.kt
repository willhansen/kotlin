// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT
// WITH_HELPERS

import helpers.*

fun box(): String {
    fun foo(): Any {
        return {}
    }

    konst javaClass = foo().javaClass

    // The enclosing method is a local function, which are in a separate class (implementing FunctionN) for non-IR, and are static methods
    // in the enclosing class for IR.
    konst actualEnclosingMethod = javaClass.getEnclosingMethod()!!.getName()
    konst expectedEnclosingMethod = if (isIR()) "box\$foo" else "invoke"
    if (actualEnclosingMethod != expectedEnclosingMethod) return "method: $actualEnclosingMethod"

    konst actualEnclosingClass = javaClass.getEnclosingClass()!!.getName()
    konst expectedEnclosingClass = if (isIR()) "LambdaInLocalFunctionKt" else "LambdaInLocalFunctionKt\$box\$1"
    if (actualEnclosingClass != expectedEnclosingClass) return "enclosing class: $actualEnclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
