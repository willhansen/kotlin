annotation class Annotation(vararg konst strings: String)

annotation class AnnotationInner(konst konstue: Annotation)

<expr>@AnnotationInner(Annotation("v1", "v2"))</expr>
class C
