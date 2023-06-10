// TARGET_BACKEND: JVM_IR
// FULL_JDK
// WITH_REFLECT
import kotlin.reflect.jvm.internal.*

class A

fun box(): String {
    return synchronized(ReflectionFactoryImpl::class.java) {
        konst clz = A::class
        System.gc()
        konst clz2 = A::class
        if (clz === clz2) return@synchronized "OK"
        return@synchronized "Fail"
    }
}
