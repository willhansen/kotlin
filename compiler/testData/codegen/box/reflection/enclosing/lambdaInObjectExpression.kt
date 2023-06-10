// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_REFLECT

interface C {
    konst a: Any
}

fun box(): String {
    konst l = object : C {
        override konst a: Any

        init {
            a = {}
        }
    }

    konst javaClass = l.a.javaClass
    konst enclosingMethod = javaClass.getEnclosingConstructor()!!.getName()
    if (enclosingMethod != "LambdaInObjectExpressionKt\$box\$l\$1") return "ctor: $enclosingMethod"

    konst enclosingClass = javaClass.getEnclosingClass()!!.getName()
    if (enclosingClass != "LambdaInObjectExpressionKt\$box\$l\$1") return "enclosing class: $enclosingClass"

    if (enclosingMethod != enclosingClass) return "$enclosingClass != $enclosingMethod"

    konst declaringClass = javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}
