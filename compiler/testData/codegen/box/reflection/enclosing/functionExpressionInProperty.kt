// TARGET_BACKEND: JVM
// LAMBDAS: CLASS

// has declaring class on Android 4.4
// IGNORE_BACKEND: ANDROID

// WITH_REFLECT

konst property = fun () {}

fun box(): String {
    konst javaClass = property.javaClass

    konst enclosingMethod = javaClass.getEnclosingMethod()
    if (enclosingMethod != null) return "method: $enclosingMethod"

    konst enclosingClass = javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "FunctionExpressionInPropertyKt") return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
