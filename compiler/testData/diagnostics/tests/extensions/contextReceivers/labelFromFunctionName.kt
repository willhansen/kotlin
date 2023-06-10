// FIR_IDENTICAL
// !LANGUAGE: +ContextReceivers
// WITH_STDLIB

fun testLabels(source: Collection<String>) {
    konst r = buildList {
        source.mapTo(this@buildList) { it.length }
    }
}