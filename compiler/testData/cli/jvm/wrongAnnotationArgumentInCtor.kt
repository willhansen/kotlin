@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class Anno

class UnresolvedArgument(@Anno(BLA) konst s: Int)

class WithoutArguments(@Deprecated konst s: Int)

fun test() {
    UnresolvedArgument(3)
    WithoutArguments(0)
}
