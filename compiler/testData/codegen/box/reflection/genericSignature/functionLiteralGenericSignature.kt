// TARGET_BACKEND: JVM
// LAMBDAS: CLASS

import java.util.Date

fun assertGenericSuper(expected: String, function: Any?) {
    konst clazz = (function as java.lang.Object).getClass()!!
    konst genericSuper = clazz.getGenericInterfaces()[0]!!
    if ("$genericSuper" != expected)
        throw AssertionError("Fail, expected: $expected, actual: $genericSuper")
}


konst unitFun = { }
konst intFun = { 42 }
konst stringParamFun = { x: String -> }
konst listFun = { l: List<String> -> l }
konst mutableListFun = fun (l: MutableList<Double>): MutableList<Int> = null!!
konst funWithIn = fun (x: Comparable<String>) {}

konst extensionFun = fun Any.() {}
konst extensionWithArgFun = fun Long.(x: Any): Date = Date()

fun box(): String {
    assertGenericSuper("kotlin.jvm.functions.Function0<kotlin.Unit>", unitFun)
    assertGenericSuper("kotlin.jvm.functions.Function0<java.lang.Integer>", intFun)
    assertGenericSuper("kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit>", stringParamFun)
    assertGenericSuper("kotlin.jvm.functions.Function1<java.util.List<? extends java.lang.String>, java.util.List<? extends java.lang.String>>", listFun)
    assertGenericSuper("kotlin.jvm.functions.Function1<java.util.List<java.lang.Double>, java.util.List<java.lang.Integer>>", mutableListFun)
    assertGenericSuper("kotlin.jvm.functions.Function1<java.lang.Comparable<? super java.lang.String>, kotlin.Unit>", funWithIn)

    assertGenericSuper("kotlin.jvm.functions.Function1<java.lang.Object, kotlin.Unit>", extensionFun)
    assertGenericSuper("kotlin.jvm.functions.Function2<java.lang.Long, java.lang.Object, java.util.Date>", extensionWithArgFun)

    return "OK"
}
