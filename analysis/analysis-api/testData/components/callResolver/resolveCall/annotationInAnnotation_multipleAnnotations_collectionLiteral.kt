annotation class Annotation(vararg konst strings: String)

annotation class AnnotationArray(vararg konst annos: Annotation)

<expr>@AnnotationArray([Annotation("v1", "v2"), Annotation(["v3", "v4"])])</expr>
class C
