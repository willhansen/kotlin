// FIR_IDENTICAL
// ISSUE: KT-41308, KT-47830

fun main() {
    sequence {
        konst list: List<String>? = null
        konst outputList = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.collections.List<kotlin.String>")!>list ?: listOf()<!>
        yieldAll(outputList)
    }
}
