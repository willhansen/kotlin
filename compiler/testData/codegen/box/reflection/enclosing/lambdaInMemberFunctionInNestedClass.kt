// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

class C {
    class D {
        fun foo(): Any {
            return {}
        }
    }
}

fun box(): String {
    konst javaClass = C.D().foo().javaClass
    konst enclosingMethod = javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "foo") return "method: $enclosingMethod"

    konst enclosingClass = javaClass.getEnclosingClass()
    if (enclosingClass?.getSimpleName() != "D") return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
