package a

import kotlin.reflect.KClass

interface A {
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE_PARAMETER)
    annotation class Anno(konst konstue: String)
}

annotation class K(konst klass: KClass<*>)
