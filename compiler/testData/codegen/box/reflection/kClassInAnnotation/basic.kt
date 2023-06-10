// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(konst arg: KClass<*>)

class OK

@Ann(OK::class) class MyClass

fun box(): String {
    konst argName = MyClass::class.java.getAnnotation(Ann::class.java).arg.simpleName ?: "fail 1"
    return argName
}
