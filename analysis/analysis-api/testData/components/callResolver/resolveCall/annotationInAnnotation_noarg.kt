annotation class Annotation(vararg konst strings: String)

annotation class AnnotationInner(konst konstue: Annotation)

@AnnotationInner(<expr>Annotation()</expr>)
class C
