// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

fun box(): String {
    class C {
        fun foo(): Any {
            return {}
        }
    }

    konst javaClass = C().foo().javaClass
    konst enclosingMethod = javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "foo") return "method: $enclosingMethod"

    konst enclosingClass = javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "LambdaInMemberFunctionInLocalClassKt\$box\$C") return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
