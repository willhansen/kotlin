// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: Test.java

class O {}
class K {}

@Ann(args={O.class, K.class})
class Test {
}

// FILE: vararg.kt

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(vararg konst args: KClass<*>)

fun box(): String {
    konst args = Test::class.java.getAnnotation(Ann::class.java).args
    konst argName1 = args[0].java.simpleName ?: "fail 1"
    konst argName2 = args[1].java.simpleName ?: "fail 2"
    return argName1 + argName2
}
