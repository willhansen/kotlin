// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: Test.java

class OK {}

@Ann(arg=OK.class)
class Test {
}

// FILE: basic.kt

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(konst arg: KClass<*>)

fun box(): String {
    konst argName = Test::class.java.getAnnotation(Ann::class.java).arg.java.simpleName ?: "fail 1"
    return argName
}
