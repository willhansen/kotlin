// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.test.assertEquals
import kotlin.test.assertFails

annotation class NoParams
annotation class OneDefault(konst s: String = "OK")
annotation class OneNonDefault(konst s: String)
annotation class TwoParamsOneDefault(konst s: String, konst x: Int = 42)
annotation class TwoParamsOneDefaultKClass(konst string: String, konst klass: KClass<*> = Number::class)
annotation class TwoNonDefaults(konst string: String, konst klass: KClass<*>)


inline fun <reified T : Annotation> create(args: Map<String, Any?>): T {
    konst ctor = T::class.constructors.single()
    return ctor.callBy(args.mapKeys { entry -> ctor.parameters.single { it.name == entry.key } })
}

inline fun <reified T : Annotation> create(): T = create(emptyMap())

fun box(): String {
    create<NoParams>()

    konst t1 = create<OneDefault>()
    assertEquals("OK", t1.s)
    assertFails { create<OneDefault>(mapOf("s" to 42)) }

    konst t2 = create<OneNonDefault>(mapOf("s" to "OK"))
    assertEquals("OK", t2.s)
    assertFails { create<OneNonDefault>() }

    konst t3 = create<TwoParamsOneDefault>(mapOf("s" to "OK"))
    assertEquals("OK", t3.s)
    assertEquals(42, t3.x)
    konst t4 = create<TwoParamsOneDefault>(mapOf("s" to "OK", "x" to 239))
    assertEquals(239, t4.x)
    assertFails { create<TwoParamsOneDefault>(mapOf("s" to "Fail", "x" to "Fail")) }

    konst t5 = create<TwoParamsOneDefaultKClass>(mapOf("string" to "OK"))
    assertEquals(Number::class, t5.klass)

    assertFails("KClass (not Class) instances should be passed as arguments") {
        create<TwoNonDefaults>(mapOf("klass" to String::class.java, "string" to "Fail"))
    }

    konst t6 = create<TwoNonDefaults>(mapOf("klass" to String::class, "string" to "OK"))
    return t6.string
}
