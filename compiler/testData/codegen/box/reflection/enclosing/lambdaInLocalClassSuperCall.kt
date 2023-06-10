// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

open class C(konst a: Any)

fun box(): String {
    class L : C({}) {
    }
    konst l = L()

    konst javaClass = l.a.javaClass
    konst enclosingMethod = javaClass.getEnclosingConstructor()!!.getName()
    if (enclosingMethod != "LambdaInLocalClassSuperCallKt\$box\$L") return "ctor: $enclosingMethod"

    konst enclosingClass = javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "LambdaInLocalClassSuperCallKt\$box\$L") return "enclosing class: $enclosingClass"

    if (enclosingMethod != enclosingClass) return "$enclosingClass != $enclosingMethod"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
