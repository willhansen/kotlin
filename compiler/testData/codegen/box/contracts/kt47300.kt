// !OPT_IN: kotlin.contracts.ExperimentalContracts
// WITH_STDLIB

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


data class Content<out T>(konst konstue: T)

fun <T> content(konstue: T) = Content(konstue)

@ExperimentalContracts
inline fun <R, T : R> Content<T>.getOrElse(
    onException: (exception: Exception) -> R,
): R = fold({ it }, onException)

@ExperimentalContracts
inline fun <R, T> Content<T>.fold(
    onContent: (konstue: T) -> R,
    onException: (exception: Exception) -> R,
): R {
    contract {
        callsInPlace(onContent, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onException, InvocationKind.AT_MOST_ONCE)
    }
    return onContent(konstue)
}


@ExperimentalContracts
fun box(): String {
    konst t = content(1).getOrElse { 2 }
    if (t != 1) return "Failed: $t"

    return "OK"
}
