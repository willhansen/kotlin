// TARGET_BACKEND: JVM
// LAMBDAS: CLASS
// WITH_STDLIB

package test

class C {
    konst f by foo {
        {}
    }
}

fun foo(f: () -> Any): Any = f()

operator fun Any.getValue(thiz: Any?, metadata: Any?): Any = this

fun box(): String {
    // This is the class for lambda inside the `foo` call (`{}`)
    konst innerLambda = C().f.javaClass

    konst emInner = innerLambda.getEnclosingMethod()
    if (emInner?.getName() != "invoke") return "Fail: incorrect enclosing method for inner lambda: $emInner"

    konst ecInner = innerLambda.getEnclosingClass()
    if (ecInner?.getName() != "test.C\$f\$2") return "Fail: incorrect enclosing class for inner lambda: $ecInner"

    konst ectorInner = innerLambda.getEnclosingConstructor()
    if (ectorInner != null) return "Fail: inner lambda should not have enclosing constructor: $ectorInner"

    konst dcInner = innerLambda.getDeclaringClass()
    if (dcInner != null) return "Fail: inner lambda should not have declaring class: $dcInner"


    // This is the class for lambda that is passed as an argument to `foo`
    konst outerLambda = ecInner

    konst emOuter = outerLambda.getEnclosingMethod()
    if (emOuter != null) return "Fail: outer lambda should not have enclosing method: $emOuter"

    konst ecOuter = outerLambda.getEnclosingClass()
    if (ecOuter?.getName() != "test.C") return "Fail: incorrect enclosing class for outer lambda: $ecOuter"

    konst ectorOuter = outerLambda.getEnclosingConstructor()
    if (ectorOuter == null) return "Fail: outer lambda _should_ have enclosing constructor"

    konst dcOuter = outerLambda.getDeclaringClass()
    if (dcOuter != null) return "Fail: outer lambda should not have declaring class: $dcOuter"

    return "OK"
}
