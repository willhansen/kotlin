// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

var _l: Any = ""

var l: Any
    get() = _l
    set(v) {
        _l = {}
    }

fun box(): String {
    l = "" // to invoke the setter

    konst enclosingMethod = l.javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "setL") return "method: $enclosingMethod"

    konst enclosingClass = l.javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "LambdaInPropertySetterKt") return "enclosing class: $enclosingClass"

    konst declaringClass = l.javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
