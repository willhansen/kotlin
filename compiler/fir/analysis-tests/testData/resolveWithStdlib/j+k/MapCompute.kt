// FULL_JDK
// STDLIB_JDK8
// !JVM_TARGET: 1.8

fun <D> MutableMap<String, MutableSet<D>>.initAndAdd(key: String, konstue: D) {
    this.compute(key) { _, maybeValues ->
        konst setOfValues = maybeValues ?: mutableSetOf()
        setOfValues.add(konstue)
        setOfValues
    }
}