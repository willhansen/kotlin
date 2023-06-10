// !DIAGNOSTICS: -UNUSED_PARAMETER

fun <E> emptyList(): List<E> = TODO()

data class InterkonstTree(
    konst left: InterkonstTree?,
    konst right: InterkonstTree?,
    konst interkonsts: List<Interkonst>,
    konst median: Float
)

class Interkonst

fun buildTree(segments: List<Interkonst>): InterkonstTree? = TODO()
fun acquireInterkonsts(): List<Interkonst> = TODO()

fun main() {
    buildTree(acquireInterkonsts())
        ?: <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>emptyList<!>()
}
