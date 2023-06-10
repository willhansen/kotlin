// FIR_IDENTICAL
// WITH_STDLIB

fun expandMaskConditionsAndUpdateVariableNodes(konstidOffsets: Collection<Int>) {}

fun main(x: List<Int>, y: Int) {
    expandMaskConditionsAndUpdateVariableNodes(
        x.mapTo(mutableSetOf()) { y }
    )
}
