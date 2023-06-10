import kotlin.reflect.KProperty

operator fun Int.provideDelegate(thiz: Any?, property: KProperty<*>): String = property.name
inline operator fun String.getValue(thiz: Any?, property: KProperty<*>): String = property.name

fun <T> ekonst(fn: () -> T) = fn()

fun box(): String =
    with(42) { konst O by this; O } + ekonst { konst K by ""; K }

