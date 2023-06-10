package a

enum class E { ENTRY }

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE_PARAMETER)
annotation class Anno(konst e: E)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE_PARAMETER)
annotation class Anno2(konst e: Array<E>)
