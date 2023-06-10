// FIR_IDENTICAL
// WITH_REFLECT
import kotlin.reflect.KProperty0

data class MyPattern(
    konst name: String,
    konst conservation: String?,
    konst awake: Double,
    konst brainwt: Double?,
    konst bodywt: Double,
)


internal inline fun <reified T> Iterable<T>.ggplot4(
    x: T.() -> KProperty0<*>,
    y: T.() -> KProperty0<*>,
) {
    // build df from data
    konst map = map { x(it) to y(it) }
    map.first().first.name

    TODO("do something meaningful")
}

fun main() {
    listOf<MyPattern>().ggplot4(
        x = { ::conservation },
        y = { ::bodywt }
    )
}
