import kotlin.reflect.KClass

annotation class SimpleAnn(konst konstue: String)

annotation class Ann(
    konst x: Int,
    konst y: String,
    konst z: KClass<*>,
    konst e: Array<KClass<*>>,
    konst depr: DeprecationLevel,
    vararg konst t: SimpleAnn
)

interface Base {
    @Ann(1, "134", String::class, arrayOf(Int::class, Double::class), DeprecationLevel.WARNING, SimpleAnn("243"), SimpleAnn("4324"))
    fun foo(
        @Ann(2, "324", Ann::class, arrayOf(Byte::class, Base::class), DeprecationLevel.WARNING, SimpleAnn("687"), SimpleAnn("78")) x: String
    )
}

class Derived(b: Base) : Base by b {

}
