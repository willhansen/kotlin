// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.experimental.ExperimentalTypeInference

interface Build<T>

@OptIn(ExperimentalTypeInference::class)
fun <T> build(fn: Builder<T>.() -> Unit): Build<T> = TODO()

// Works completely
konst build = build {
    konstue(1)
}

// Works completely
konst buildWithWrappedValue = build {
    wrappedValue(Wrapped(1))
}

// Works completely
konst buildWithFn = build {
    konstueFn {
        1
    }
}

// Works, but the ide complains with "Non-applicable call for builder inference"
konst buildWithFnWrapped = build {
    wrappedValueFn {
        Wrapped(1)
    }
}

interface Builder<T> {
    fun konstue(konstue: T)
    fun wrappedValue(konstue: Wrapped<T>)
    fun wrappedValueFn(fn: () -> Wrapped<T>)
    fun konstueFn(fn: () -> T)
}

data class Wrapped<T>(konst konstue: T)