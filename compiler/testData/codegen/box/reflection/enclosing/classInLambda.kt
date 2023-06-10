// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

fun box(): String {

    konst lambda = {
        class Z {}
        Z()
    }

    konst classInLambda = lambda()

    konst enclosingMethod = classInLambda.javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "invoke") return "method: $enclosingMethod"

    konst enclosingClass = classInLambda.javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "ClassInLambdaKt\$box\$lambda\$1") return "enclosing class: $enclosingClass"

    konst declaringClass = classInLambda.javaClass.getDeclaringClass()
    if (declaringClass != null) return "class has a declaring class"

    return "OK"
}
