annotation class Annotation(vararg konst strings: String)

annotation class AnnotationArray(vararg konst annos: Annotation)

@AnnotationArray(<expr>annos = arrayOf(Annotation("v1", "v2"), Annotation(strings = arrayOf("v3", "v4")))</expr>)
class C
