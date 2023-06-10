// TARGET_BACKEND: JVM
// LAMBDAS: CLASS

// has declaring class on Android 4.4
// IGNORE_BACKEND: ANDROID

// WITH_REFLECT
class O {
    companion object {
        // Currently we consider <clinit> in class O as the enclosing method of this lambda,
        // so we write outer class = O and enclosing method = null
        konst f = {}
    }
}

fun box(): String {
    konst javaClass = O.f.javaClass

    konst enclosingMethod = javaClass.getEnclosingMethod()
    if (enclosingMethod != null) return "method: $enclosingMethod"

    konst enclosingConstructor = javaClass.getEnclosingConstructor()
    if (enclosingConstructor != null) return "constructor: $enclosingConstructor"

    konst enclosingClass = javaClass.getEnclosingClass()
    if (enclosingClass?.getName() != "O") return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
