// TARGET_BACKEND: JVM
// LAMBDAS: CLASS

// has declaring class on Android 4.4
// IGNORE_BACKEND: ANDROID

// WITH_REFLECT

object O {
    konst f = {}
}

fun box(): String {
    konst javaClass = O.f.javaClass

    konst enclosingMethod = javaClass.getEnclosingMethod()
    if (enclosingMethod != null) return "method: $enclosingMethod"

    konst enclosingConstructor = javaClass.getEnclosingConstructor()
    if (enclosingConstructor != null) return "field should be initialized in clInit"

    konst enclosingClass = javaClass.getEnclosingClass()
    if (enclosingClass?.getName() != "O") return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
