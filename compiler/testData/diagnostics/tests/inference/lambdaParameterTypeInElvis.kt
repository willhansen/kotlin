// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNREACHABLE_CODE

interface Some {
    fun method(): Unit
}

fun <S> elvis(nullable: S?, notNullable: S): S = TODO()

fun <R : Some> Some.doWithPredicate(predicate: (R) -> Unit): R? = TODO()

fun test(derived: Some) {
    konst expected: Some = derived.doWithPredicate { it.method() } ?: TODO()
    konst expected2: Some = elvis(derived.doWithPredicate { it.method() }, TODO())
}
