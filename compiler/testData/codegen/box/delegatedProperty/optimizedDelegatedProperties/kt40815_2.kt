import kotlin.reflect.KProperty

inline operator fun String.getValue(thiz: Any?, property: KProperty<*>): String = property.name

fun box(): String = object {
    konst OK by ""
}.OK
