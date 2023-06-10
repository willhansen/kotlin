// !LANGUAGE: +ProperCheckAnnotationsTargetInTypeUsePositions
// !DIAGNOSTICS: -USELESS_CAST
// ISSUE: KT-46173

@Target(AnnotationTarget.TYPE)
annotation class Ann(konst s: String)

fun some(): Int {
    return 1 as @Ann(<!ARGUMENT_TYPE_MISMATCH!>6<!>) Int // should error but doesn't
}
