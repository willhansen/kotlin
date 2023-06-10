package test

annotation class Empty

annotation class JustAnnotation(konst annotation: Empty)

annotation class AnnotationArray(konst annotationArray: Array<JustAnnotation>)

@JustAnnotation(Empty())
@AnnotationArray(arrayOf())
class C1

@AnnotationArray(arrayOf(JustAnnotation(Empty()), JustAnnotation(Empty())))
class C2
