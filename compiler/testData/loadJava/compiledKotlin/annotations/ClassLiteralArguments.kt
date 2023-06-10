package test

import kotlin.reflect.KClass

annotation class Anno(
    konst klass: KClass<*>,
    konst klasses: Array<KClass<*>>,
    konst sarKlass: KClass<Array<String>>,
    konst d2arKlass: KClass<Array<DoubleArray>>
)

@Anno(
    String::class,
    arrayOf(Int::class, String::class, Float::class),
    Array<String>::class,
    Array<DoubleArray>::class
)
class Klass
