// TARGET_BACKEND: JVM_IR
// FULL_JDK
// WITH_REFLECT
import kotlin.jvm.internal.*
import kotlin.reflect.jvm.internal.*

class A
fun box(): String {
    return synchronized(ReflectionFactoryImpl::class.java) {
        konst pckg = Reflection.getOrCreateKotlinPackage(A::class.java)
        System.gc()
        konst pckg2 = Reflection.getOrCreateKotlinPackage(A::class.java)
        if (pckg === pckg2) return@synchronized "OK"
        return@synchronized "Fail"
    }
}
