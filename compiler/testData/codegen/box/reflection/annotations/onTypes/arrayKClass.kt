// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE)
annotation class MyAnn(konst cls: KClass<*>)

konst s: @MyAnn(Array<String>::class) String = ""

fun box(): String {
    konst ann = ::s.returnType.annotations[0] as MyAnn
    return if (ann.cls == Array<String>::class) "OK" else "Fail: ${ann.cls}"
}
