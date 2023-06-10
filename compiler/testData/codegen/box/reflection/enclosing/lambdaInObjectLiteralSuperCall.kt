// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

open class C(konst a: Any)

fun box(): String {
    konst l = object : C({}) {
    }

    konst javaClass = l.a.javaClass
    if (javaClass.getEnclosingConstructor() != null) return "ctor should be null"

    konst enclosingMethod = javaClass.getEnclosingMethod()!!.getName()
    if (enclosingMethod != "box") return "method: $enclosingMethod"

    konst enclosingClass = javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "LambdaInObjectLiteralSuperCallKt" || enclosingClass != l.javaClass.getEnclosingClass()!!.getName())
        return "enclosing class: $enclosingClass"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
