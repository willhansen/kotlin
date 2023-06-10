// TARGET_BACKEND: JVM
// LAMBDAS: CLASS

// has declaring class on Android 4.4
// IGNORE_BACKEND: ANDROID

// WITH_REFLECT

konst l: Any = {}

fun box(): String {
    konst enclosingClass = l.javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "LambdaInPackageKt") return "enclosing class: $enclosingClass"

    konst enclosingConstructor = l.javaClass.getEnclosingConstructor()
    if (enclosingConstructor != null) return "enclosing constructor found: $enclosingConstructor"

    konst enclosingMethod = l.javaClass.getEnclosingMethod()
    if (enclosingMethod != null) return "enclosing method found: $enclosingMethod"

    konst declaringClass = l.javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
