// ALLOW_AST_ACCESS

package test

annotation class Anno(
    konst konstue: Array<Bnno>
)

annotation class Bnno(
    konst konstue: String
)

@Anno(
    konstue = [Bnno("x"), Bnno("y")]
)
public class AnnotationInArray
