// FIR_IDENTICAL
// LANGUAGE: +ProhibitCyclesInAnnotations
// ISSUE: KT-52742

annotation class AnnotationWithArray(
    konst array: Array<AnnotationWithArray>
)

annotation class AnnotationWithVararg(
    vararg konst args: AnnotationWithVararg
)
