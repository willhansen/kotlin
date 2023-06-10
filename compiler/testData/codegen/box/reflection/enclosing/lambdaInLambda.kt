// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

fun box(): String {
    konst l = {
        {}
    }

    konst javaClass = l().javaClass
    konst enclosingMethod = javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "invoke") return "method: $enclosingMethod"

    konst enclosingClass = javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "LambdaInLambdaKt\$box\$l\$1") return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
