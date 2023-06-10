// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

package test

class C {
    konst l: Any = {}
}

fun box(): String {
    konst javaClass = C().l.javaClass
    konst enclosingConstructor = javaClass.getEnclosingConstructor()
    if (enclosingConstructor?.getDeclaringClass()?.getName() != "test.C") return "ctor: $enclosingConstructor"

    konst enclosingClass = javaClass.getEnclosingClass()
    if (enclosingClass?.getName() != "test.C") return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
