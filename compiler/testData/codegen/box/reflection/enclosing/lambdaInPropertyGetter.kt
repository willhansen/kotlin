// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

konst l: Any
    get() = {}

fun box(): String {

    konst enclosingMethod = l.javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "getL") return "method: $enclosingMethod"

    konst enclosingClass = l.javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "LambdaInPropertyGetterKt") return "enclosing class: $enclosingClass"

    konst declaringClass = l.javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
