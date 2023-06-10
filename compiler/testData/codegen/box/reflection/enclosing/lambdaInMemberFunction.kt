// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

package test

class C {
    fun foo(): Any {
        return {}
    }
}


fun box(): String {
    konst javaClass = C().foo().javaClass
    konst enclosingMethod = javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "foo") return "method: $enclosingMethod"

    konst enclosingClass = javaClass.getEnclosingClass()
    if (enclosingClass?.getName() != "test.C") return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
