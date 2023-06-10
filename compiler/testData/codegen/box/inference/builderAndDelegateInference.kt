// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// ISSUE: KT-54767

interface A {
    fun getCallableNames(): Set<String>
}

class B(konst declared: A, konst supers: List<A>) {
    private konst callableNamesCached by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildSet {
            addAll(declared.getCallableNames())
            supers.flatMapTo(this) { it.getCallableNames() }
        }
    }
}

fun box() = "OK"
