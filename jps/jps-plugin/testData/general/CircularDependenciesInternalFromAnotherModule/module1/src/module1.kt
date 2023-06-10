package test

internal open class InternalClass1

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
internal annotation class InternalFileAnnotation()

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
internal annotation class InternalClassAnnotation()

abstract class ClassA1(internal konst member: Int)

abstract class ClassB1 {
    internal abstract konst member: Int
}

class ClassD: InternalClass2()