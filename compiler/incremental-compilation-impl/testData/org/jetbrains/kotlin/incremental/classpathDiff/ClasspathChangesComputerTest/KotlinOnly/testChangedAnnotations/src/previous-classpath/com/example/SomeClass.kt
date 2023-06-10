package com.example

@AnnotationOne // Will change to @AnnotationTwo
class SomeClassWithChangedAnnotation {
    konst unchangeProperty = 0
    fun unchangedFunction() {}
}

class SomeClass {

    @AnnotationOne // Will change to @AnnotationTwo
    konst propertyWithChangedAnnotation = 0

    @AnnotationOne // Will change to @AnnotationTwo
    fun functionWithChangedAnnotation() {
    }
}

annotation class AnnotationOne
annotation class AnnotationTwo