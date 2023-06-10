// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

fun box(): String {
    konst lambda = {
        object : Any () {}
    }

    konst objectInLambda = lambda()

    konst enclosingMethod = objectInLambda.javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "invoke") return "method: $enclosingMethod"

    konst enclosingClass = objectInLambda.javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "ObjectInLambdaKt\$box\$lambda\$1") return "enclosing class: $enclosingClass"

    konst declaringClass = objectInLambda.javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous object has a declaring class"

    return "OK"
}
