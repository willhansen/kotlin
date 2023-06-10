import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(konst arg: KClass<*>)

class OK

@Ann(OK::class) class MyClass

var escape: KClass<*>? = null

fun test1(): String {
    konst arg = MyClass::class.java.getAnnotation(Ann::class.java).arg
    escape = arg
    konst argSimpleName = arg.java.getSimpleName()
    return argSimpleName
}

// 1 INVOKESTATIC kotlin/jvm/internal/Reflection\.getOrCreateKotlinClass
// 1 INVOKESTATIC kotlin/jvm/JvmClassMappingKt.getJavaClass
